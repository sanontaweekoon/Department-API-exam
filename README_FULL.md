## Startup order and health checks

### ลำดับการ start

```
postgres ──healthy──▶ app1, app2 ──healthy──▶ nginx

loki ──healthy──▶ alloy
prometheus, loki ──healthy──▶ grafana
```

ลำดับนี้กำหนดด้วย `depends_on` + `condition: service_healthy` ใน `docker-compose.yml`
service ถัดไปจะ start เมื่อ healthcheck ของตัวก่อนหน้า **ผ่าน** ไม่ใช่แค่ container started

| ขั้น | เหตุผล | ถ้าไม่รอจะเกิดอะไร |
|---|---|---|
| postgres → app1, app2 | app ต้องสร้าง connection pool และ Hibernate `validate` schema ตอน start | app ได้ `Connection refused` และ start ไม่ขึ้น |
| app1, app2 → nginx | NGINX resolve ชื่อ `app1` / `app2` ใน upstream ตอน start | nginx start ไม่ขึ้น: `host not found in upstream "app1:8200"` |
| loki → alloy | alloy ส่ง log เข้า loki | log ช่วงแรกส่งไม่สำเร็จ |
| prometheus, loki → grafana | grafana โหลด datasource ทั้งสองตัวตอน provisioning | datasource ใช้งานไม่ได้ช่วงแรก |

### Healthcheck แต่ละ service

| Service | คำสั่ง | interval | timeout | retries | start_period |
|---|---|---|---|---|---|
| postgres | `pg_isready -U exam_user -d examdb` | 5s | 5s | 5 | 10s |
| app1 / app2 | `wget` → `/actuator/health/readiness` | 15s | 5s | 5 | 90s |
| nginx | `wget` → `/nginx-health` | 15s | 5s | 3 | 10s |
| prometheus | `wget` → `/-/ready` | 15s | 5s | 5 | 20s |
| loki | `wget` → `/ready` | 15s | 5s | 5 | 30s |
| grafana | `wget` → `/api/health` | 15s | 5s | 5 | 30s |
| alloy | `bash` + `/dev/tcp` → `/-/ready` | 15s | 5s | 5 | 20s |

**คำสั่งที่มีจริงใน image:** `wget` มาจาก BusyBox ใน image alpine และ `pg_isready` มากับ postgres image
image ของ alloy ไม่มี `wget`/`curl` จึงใช้ `bash` เปิด TCP ส่ง HTTP request แทน
ไม่ต้องติดตั้งเครื่องมือเพิ่มใน Dockerfile

**เหตุผลของค่าที่ตั้ง (วัดจากของจริง):**
- **app start_period 90s:** Spring Boot start จริง ~<X> วินาที
  (`docker logs spring-app-2 | Select-String "Started"`) เผื่อไว้ ~3 เท่า สำหรับเครื่องที่ช้ากว่า
- **app timeout 5s:** readiness ตอบจริง ~<Y> ms (`.State.Health.Log` Start → End)
  เผื่อการสะดุดจาก GC/DB ได้ และยังน้อยกว่า interval
- **app interval 15s × retries 5:** ต้องล้มติดกัน ~75 วินาทีถึงเป็น unhealthy ปัญหาชั่วคราวจึงไม่ทำให้ flap
- **postgres interval 5s:** app ทั้งสองตัวรอ postgres อยู่ เช็กถี่เพื่อให้ app start ได้เร็วขึ้น

### Readiness vs Liveness

| Endpoint | รวม DB | ใช้ทำอะไร |
|---|---|---|
| `/actuator/health/readiness` | ✅ (`readinessState`, `db`) | Compose healthcheck: พร้อมรับ traffic ไหม |
| `/actuator/health/liveness` | ❌ | process ยังทำงานอยู่ไหม: DB ล่มชั่วคราวไม่ควรทำให้ถูกมองว่าตาย |

### ตรวจสอบลำดับการ start

```bash
docker compose down        # ลบ container แต่ไม่ลบ volume (ข้อมูล DB ยังอยู่)
docker compose up -d       # จะเห็น postgres Waiting → Healthy → app Started → nginx Started
docker compose ps -a       # ทุก service ต้องเป็น (healthy)
```

> ⚠️ ห้ามใช้ `docker compose down -v` เพราะ `-v` จะลบ volume รวมถึงข้อมูลใน `postgres_data`

### ข้อจำกัด

- `depends_on` มีผล **ตอน start เท่านั้น** ระหว่างรัน ถ้า postgres ล่ม Compose ไม่ได้หยุดหรือ restart app ตาม
- NGINX (open source) **ไม่ได้อ่าน health status ของ Docker** ระหว่างรัน
  จะรู้ว่า upstream ตายจากการส่ง request แล้วล้มเท่านั้น (`max_fails`)
  ดูรายละเอียดในหัวข้อ Load Balancer routing และ Incident
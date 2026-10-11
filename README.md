## ลำดับการ start ของระบบ

ระบบ start ตามลำดับนี้:

```
1. postgres
2. app1, app2   (รอให้ postgres พร้อมก่อน)
3. nginx        (รอให้ app1 และ app2 พร้อมก่อน)
```

ฝั่ง monitoring: `loki` start ก่อน แล้ว `alloy` และ `grafana` จึง start ตาม

### ทำไมต้องเรียงแบบนี้

- **app ต้องรอ postgres** เพราะตอน start app ต้องต่อ DB ทันที ถ้า DB ยังไม่พร้อม app จะ start ไม่ขึ้น
- **nginx ต้องรอ app** เพราะ nginx ต้องหาชื่อ `app1` และ `app2` ตอน start ถ้ายังไม่มี nginx จะ error `host not found in upstream`

ใน `docker-compose.yml` ใช้ `depends_on` กับ `condition: service_healthy`
แปลว่า "รอให้ตัวก่อนหน้า **ผ่าน healthcheck** ก่อน" ไม่ใช่แค่ container เปิดแล้ว

### Healthcheck คืออะไร

Docker จะรันคำสั่งเช็กแต่ละ container เป็นระยะ ถ้าผ่านถือว่า `healthy`

| Service | เช็กด้วย |
|---|---|
| postgres | `pg_isready` (DB รับ connection แล้วหรือยัง) |
| app1, app2 | `wget` เรียก `/actuator/health/readiness` (app พร้อมและต่อ DB ได้) |
| nginx | `wget` เรียก `/nginx-health` |

คำสั่ง `wget` และ `pg_isready` มีอยู่ใน image อยู่แล้ว ไม่ต้องติดตั้งเพิ่ม

### ค่าที่ตั้งไว้ของ app

| ค่า | ตั้งไว้ | ความหมาย |
|---|---|---|
| start_period | 90 วินาที | ช่วงแรกที่ app กำลัง start ถ้าเช็กไม่ผ่านจะยังไม่นับ (app start จริงใช้ประมาณ <X> วินาที) |
| interval | 15 วินาที | เช็กทุก 15 วินาที |
| timeout | 5 วินาที | ถ้ารอเกิน 5 วินาทีถือว่าไม่ผ่าน (ปกติตอบใน <Y> ms) |
| retries | 5 ครั้ง | ต้องไม่ผ่านติดกัน 5 ครั้งถึงจะเป็น `unhealthy` |

postgres เช็กถี่กว่า (ทุก 5 วินาที) เพราะ app รอ postgres อยู่ ยิ่งรู้เร็ว app ยิ่ง start ได้เร็ว

### วิธีตรวจสอบ

```bash
docker compose down     # หยุดและลบ container (ข้อมูลใน DB ไม่หาย)
docker compose up -d    # start ใหม่ จะเห็นลำดับ postgres → app → nginx
docker compose ps -a    # ทุกตัวต้องเป็น (healthy)
```

> ห้ามใช้ `docker compose down -v` เพราะจะลบข้อมูลใน DB ด้วย

### ข้อควรรู้

ลำดับนี้มีผล **ตอน start เท่านั้น** ระหว่างที่ระบบรันอยู่ nginx ไม่ได้ดูว่า app เป็น healthy หรือไม่
(ดูรายละเอียดในหัวข้อ Incident)


### Database (lab only)

| | ค่า |
|---|---|
| Host (ใน Docker network) | `postgres:5432` |
| Host (จากเครื่อง, dev เท่านั้น) | `localhost:5432` |
| Database | `examdb` |
| User | `exam_user` |
| Password | `exam_pass` (ตั้งใน `.env`) |

Port 5432 เปิดเฉพาะ `127.0.0.1` เพื่อใช้รันแอปจาก IDE และตรวจข้อมูลด้วย DB client
app1/app2 ไม่ได้ใช้ port นี้ แต่ต่อผ่าน Docker network ด้วยชื่อ `postgres`

### Version
|                      |                                        |
|----------------------|----------------------------------------|
| postgres             | postgres:17.11-alpine                  |
| app build / runtime	 | eclipse-temurin:21.0.12.1_1-jdk-alpine |
| nginx	               | nginx:1.31.6-alpine                  |
| prometheus           | prom/prometheus:v3.5.0                 |
| loki                 | 	grafana/loki:3.5.3                    |
| alloy	               | grafana/alloy:v1.10.2                |
| grafana              | grafana/grafana:12.1.1                 |


Build .jar

         Dockerfile (bootJar ครั้งเดียว)
                    │
                    ▼
       image: department-api:0.0.1
         (sha256:..., app.jar sha256:...)
            ┌───────┴───────┐
            ▼               ▼
      spring-app-1     spring-app-2
INSTANCE_NAME=app-1  INSTANCE_NAME=app-2   ← ต่างกันแค่ env


## Performance: GET /api/departments

| | Before | After |
|---|---|---|
| SELECT ต่อ request | 7 (1 + 1 ต่อ department: N+1) | 1 |
| เพิ่มตามจำนวน department | ใช่ | ไม่ |
| วิธี | findAll + countByDepartmentId ทีละ department | LEFT JOIN + GROUP BY + projection |

- LEFT JOIN ทำให้ department ที่ไม่มี employee (NEW) ยังอยู่ในผลลัพธ์
- COUNT(e.id) แทน COUNT(*) เพื่อให้ NEW ได้ 0 ไม่ใช่ 1
- ไม่ใช้ cache และไม่โหลด employee มานับใน memory
- Response เหมือนเดิมทุก field (contract ไม่เปลี่ยน)

วัดบน: dataset 10,000 employees / 6 departments, 1 request, รันจาก IntelliJ ต่อ Postgres ใน Docker
วิธีนับ: `logging.level.org.hibernate.SQL=debug`


## Tests

```bash
./gradlew test
# Integration Project

Proyecto Spring Boot para gestión de planes de estudio (Study Plans, Curriculums, Topics, Users).

Fecha: 2025-11-14

---

## Resumen rápido

Este README explica cómo funciona la autenticación por JWT en este proyecto, cómo realizar el login desde el frontend y ejemplos prácticos (curl/PowerShell y axios). También resume endpoints importantes relacionados con StudyPlans y Curriculums y muestra ejemplos de payloads para crear/actualizar planes con entries (fecha - topic).


## 1) Conceptos clave

- El backend usa JWT (JSON Web Tokens) para autenticar requests de cliente. El token se firma con un secreto (HS256) y contiene el `subject` (email) y una lista de `roles` como claim.
- El token de acceso es *stateless*: el servidor no lo guarda; valida la firma y la expiración en cada request.
- Para mejorar seguridad en producción se recomienda usar tokens de corta vida y refresh tokens con rotación/almacenamiento seguro (opcional, no implementado por defecto aquí).


## 2) Variables de entorno / configuración JWT

El proyecto carga propiedades de JWT desde `application.properties` usando el prefijo `jwt`. Debes definir al menos la siguiente variable de entorno (o propiedad):

- `jwt.secret` (requerido) — secreto largo para firmar HS256. Ejemplo (no poner esto en el repo):
  - PowerShell (temporal en la sesión):

```powershell
$env:JWT_SECRET = "una_clave_muy_larga_y_segura_que_no_debes_committear"
# o lanzar la app con: java -Djwt.secret="..." -jar ...
```

Opcionalmente puedes definir en `application.properties` (no recomendado en repositorios públicos):

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration-seconds=900
jwt.issuer=integration-project
```

- `jwt.expiration-seconds` por defecto es 900 (15 minutos). Ajusta según tus necesidades.


## 3) Endpoints de autenticación

- POST /auth/login
  - Request (JSON):
    {
      "email": "usuario@example.com",
      "password": "secreto"
    }
  - Response (200):
    {
      "accessToken": "<JWT>",
      "tokenType": "Bearer",
      "expiresIn": 900,
      "roles": ["ROLE_TEACHER"]
    }
  - Uso: el frontend guarda el token (preferiblemente en memoria) y lo envía en el header Authorization en requests protegidos.

- Nota: por ahora no hay endpoint `/auth/refresh` implementado por defecto en este repo; si necesitas refresh tokens podemos implementarlos (recomiendo guardarlos hashed en BD y ofrecer `/auth/refresh`).


## 4) Cómo enviar el token desde el frontend

En cada request a endpoints protegidos hay que enviar el header:

Authorization: Bearer <accessToken>

Ejemplo con axios (JS/TS):

```ts
// usando el helper api existente (axios instance)
api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
// o para una llamada puntual
await api.put(`/curriculums/${id}/review`, null, { params: { reviewerId: 12, reviewMessage: 'OK' }, headers: { Authorization: `Bearer ${accessToken}` } });
```

Ejemplo con curl en PowerShell:

```powershell
curl.exe -X GET "http://localhost:8080/study-plans/13" -H "Authorization: Bearer eyJhbGciOi..." -H "Content-Type: application/json"
```


## 5) Endpoints relevantes (resumen)

- POST /auth/login — login y obtención de JWT.
- GET /study-plans/{id} — obtener plan de estudio (protegido).
- POST /study-plans/create — crear plan con entries (no protegido si tu configuración lo permite; por defecto todas las rutas distintas a `/auth/**` requieren token).
- PUT /study-plans/{id} — actualizar plan y sus entries/topics (síncrono con payload mostrado abajo).
- PUT /curriculums/{id}/review — setear o limpiar información de review (reviewedBy, reviewedAt, reviewMessage). Acepta params: `reviewerId` y `reviewMessage`.


## 6) Payloads: crear/actualizar StudyPlan con entries (fecha-topic)

Se usa `StudyPlanDTO` en el backend. Las propiedades principales para crear/actualizar con entries son:

- title, adminNumber, schedule, notes, grades
- createdById (solo en creación)
- curriculumId (solo en creación)
- entries: array de objetos { date: "YYYY-MM-DD", topics: [ { topicId: number, description: string } ] }

Ejemplo completo para crear (POST /study-plans/create):

```json
{
  "title": "Plan Noviembre",
  "adminNumber": "123",
  "schedule": "Lunes - Martes...",
  "notes": "Observaciones",
  "grades": "1º-2º",
  "createdById": 5,
  "curriculumId": 3,
  "entries": [
    {
      "date": "2025-11-20",
      "topics": [
        { "topicId": 7, "description": "Intro a X" },
        { "topicId": 9, "description": "Ejercicios" }
      ]
    },
    {
      "date": "2025-11-27",
      "topics": [
        { "topicId": 8, "description": "Avanzado" }
      ]
    }
  ]
}
```

Ejemplo para actualizar (PUT /study-plans/{id}): mismo DTO. El backend hace "sync" por `date` y `topicId`:

- Si incluyes una entrada con la misma `date`, actualizará/añadirá topics según `topicId`.
- Si no incluyes un `topicId` que existía en la DB para esa entrada, el servicio lo elimina (orphanRemoval).
- Si quitas completamente una `date` del array, esa entrada se eliminará.

Ejemplo parcial de update:

```json
{
  "title": "Plan Noviembre - v2",
  "entries": [
    {
      "date": "2025-11-20",
      "topics": [
        { "topicId": 7, "description": "Intro actualizada" },
        { "topicId": 10, "description": "Nuevo topic" }
      ]
    }
  ]
}
```


## 7) Endpoint para review de Curriculum — uso desde frontend

Ruta: PUT `/curriculums/{id}/review`
- Parámetros query (opcional): `reviewerId` y `reviewMessage`.
- Comportamiento:
  - Si `reviewerId` es un número -> se registra reviewedBy=User(reviewerId), reviewedAt=now(), reviewMessage (puede ser null).
  - Si `reviewerId` es `null` (o se omite), el servicio limpia reviewedBy/reviewedAt/reviewMessage.

Ejemplos axios:

// poner review
```ts
await api.put(`curriculums/${id}/review`, null, { params: { reviewerId: 12, reviewMessage: 'Aprobado' } });
```

// limpiar review
```ts
await api.put(`curriculums/${id}/review`, null, { params: { reviewerId: null } });
```

Nota: el controller acepta el `reviewerId` como string y convierte la cadena "null" en null para facilitar llamadas desde frontend.


## 8) Errores comunes y debugging

- Error: "A collection with orphan deletion was no longer referenced by the owning entity instance"
  - Causa: manipulación inapropiada de listas gestionadas por JPA (reemplazar la referencia de la lista en vez de mutarla). Solución: remover/añadir elementos en la colección que JPA está gestionando; el proyecto ya aplica esta corrección en `StudyPlanService.update` y usa `orphanRemoval=true` para `PlanEntry.topics`.

- Si ves que al actualizar un StudyPlan se borran todas las clases, asegúrate de que tu payload `entries` no esté vacío accidentalmente — si envías `entries: []` el servicio interpretará que no quieres ninguna entrada y las eliminará.


## 9) Recomendaciones de seguridad

- No comites `jwt.secret` ni contraseñas en el repositorio. Usar variables de entorno o servicios de secretos.
- Usar HTTPS en producción.
- Mantener `accessToken` de corta vida (ej. 15m). Implementar refresh tokens si necesitas sesiones más largas.
- Guardar refresh tokens en HttpOnly cookies o en la BD hashed con rotación.
- Para rutas sensibles, limitar por roles (ROLE_ADMIN, ROLE_TEACHER, ROLE_SECRETARY). Puedes añadir reglas en `SecurityConfig`.


## 10) Cómo probar manualmente (quick steps)

1. Establece variable de entorno JWT_SECRET y configura DB en `application.properties`.
2. Inicia la app:
```powershell
./gradlew bootRun
```
3. Login (PowerShell + curl):
```powershell
$body = '{"email":"admin@example.com","password":"secreto"}'
curl.exe -X POST "http://localhost:8080/auth/login" -H "Content-Type: application/json" -d $body
```
4. Copia `accessToken` de la respuesta y haz una petición protegida:
```powershell
curl.exe -X GET "http://localhost:8080/study-plans/13" -H "Authorization: Bearer <accessToken>"
```

## 11) Siguientes pasos recomendados

- Implementar refresh tokens con endpoint `/auth/refresh` y almacenamiento de refresh tokens en BD (hashed + expiración + rotación).
- Añadir endpoints de registro y administración de roles (admin-only).
- Añadir tests de integración para `StudyPlanService.update` (casos: añadir topic, actualizar descripción, eliminar topic/entry).
- Añadir ejemplos de Postman / colección OpenAPI.

---

Fin del README.

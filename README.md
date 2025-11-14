# Integration Project

Proyecto Spring Boot para gestión de planes de estudio (Study Plans, Curriculums, Topics, Users).

Fecha: 2025-11-14

## Resumen

Aplicación backend en Java (Spring Boot, JPA/Hibernate, Gradle) que maneja usuarios, currículos, topics y planes de estudio. Proporciona API REST para crear/leer/actualizar/eliminar recursos y funciones administrativas (cambio de contraseña, revisión de currículums, envío de correos).

## Stack tecnológico

- Java 17+ (o compatible)
- Spring Boot
- Spring Data JPA (Hibernate)
- Gradle (wrapper incluido)
- Base de datos relacional (configurable mediante `application.properties`)
- Jackson para JSON

## Estructura principal

- `src/main/java/com/example/Integration/project` - código fuente
- `src/main/resources/application.properties` - configuración
- `build.gradle` / `gradlew` - build

## Requisitos previos

- JDK 17+
- Gradle wrapper (incluido)
- Base de datos (Postgres/MySQL/otros) y credenciales configuradas en `application.properties`

## Variables de entorno / Configuración

Ajusta `src/main/resources/application.properties` con parámetros de conexión a BD y SMTP. Ejemplos:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dbname
spring.datasource.username=user
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update

# SMTP (ejemplo)
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=email@example.com
spring.mail.password=emailpassword
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Ejecutar la aplicación (desarrollo)

Desde la raíz del proyecto, en Windows Powershell:

```powershell
./gradlew bootRun
```

O para construir la JAR y ejecutarlo:

```powershell
./gradlew clean build
java -jar build/libs/Integration-project-0.0.1-SNAPSHOT.jar
```

## Tests

Ejecutar tests unitarios/integración:

```powershell
./gradlew test
```

Reporte de cobertura y otros artefactos de build aparecerán en `build/reports`.

## Endpoints importantes (resumen)

- POST `/users/set-password` - cambiar contraseña provisional. Respuestas:
  - 200 OK: contraseña cambiada (si el envío de correo falla, devuelve 200 con mensaje informando que no se pudo notificar)
  - 400/401/404/500 según caso

- GET `/study-plans` - lista de planes (por defecto devuelve entidades "raw").
- GET `/study-plans/all/raw` - endpoint que devuelve la representación completa "raw" (cuidado con serialización recursiva).

Nota: revisa los controladores en `src/main/java/com/example/Integration/project/controller` para rutas exactas y parámetros.

## Seguridad: no exponer contraseñas accidentalmente

Observación importante: en respuestas JSON no se debe enviar nunca el campo `password`. Hay dos formas seguras y recomendadas de evitarlo:

1) Marcar la propiedad como solo escritura en la entidad User para Jackson:

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String password;
```

Esto permite recibir `password` en peticiones pero evita que Jackson lo serialice en respuestas.

2) Usar DTOs para devolver solo los campos necesarios (más control y recomendado para APIs públicas).

Actualmente el proyecto incluye la solución 1 en ejemplos; verifica `src/main/java/com/example/Integration/project/entity/User.java`.

## Problema de JSON infinito (recursión) y cómo evitarlo

Si al consumir endpoints "raw" obtienes una respuesta extremadamente larga o que parece entrar en recursión, la causa habitual es que existen relaciones JPA bidireccionales (por ejemplo `StudyPlan` -> `Curriculum` -> `Topic` -> `PlanEntry` -> `StudyPlan`) y Jackson sigue navegando las referencias.

Opciones para resolverlo sin usar DTOs (manteniendo endpoints "raw"):

- Usar `@JsonManagedReference` en el lado padre y `@JsonBackReference` en el lado hijo para romper la recursión.

- Usar `@JsonIgnore` o `@JsonIgnoreProperties` en la propiedad que no quieras serializar. Por ejemplo, si en `StudyPlan` sólo quieres serializar el título del curriculum y no todo su contenido, añade en la entidad `StudyPlan`:

```java
@JsonIgnoreProperties({"topics", "createdBy", "reviewedBy", "status"})
private Curriculum curriculum;
```

Esto hará que cuando se serialice `StudyPlan`, el `curriculum` incluya solo los campos no ignorados (por ejemplo `id`, `title`, `level`) y evitará enviar listas enormes de `topics`.

- Alternativa más robusta: crear un `@JsonView` o DTO para representar el nivel de detalle.

Recomendación rápida para el caso "mostrar solo el nombre del curriculum":

- En `StudyPlan` añade `@JsonIgnoreProperties({"topics","createdBy","reviewedBy","reviewMessage","status","createdAt"})` sobre la propiedad `curriculum`. Así solo se enviarán los campos restantes (habitualmente `id`, `title`, `level`).

Ejemplo:

```java
@ManyToOne
@JoinColumn(name = "curriculum_id")
@JsonIgnoreProperties({"topics","createdBy","reviewedBy","reviewMessage","status","createdAt"})
private Curriculum curriculum;
```

## Buenas prácticas y notas de depuración

- Para desarrollo, preferir `fetch = FetchType.LAZY` en colecciones grandes para evitar cargar datos innecesarios.
- Para endpoints que devuelven colecciones grandes, paginar y devolver solo campos necesarios.
- Registrar (log) respuestas de servicios externos (p.ej. SMTP) para identificar fallos de envío sin alterar el flujo de negocio.
- Evitar exponer entidades JPA directamente en APIs públicas; los DTOs ofrecen seguridad y estabilidad del contrato.

## Contribuir

1. Abre un issue describiendo el cambio o bug.
2. Crea una rama con prefijo `feature/` o `fix/`.
3. Añade tests si cambias lógica.
4. Haz pull request con descripción clara.

## Troubleshooting rápido

- Si al cambiar contraseña el endpoint retorna "Error inesperado" pero la contraseña se actualizó, revisa que el servicio devuelva exactamente `"SUCCESS"` (o usa `equalsIgnoreCase`) y que la lógica de envío de correo maneje excepciones devolviendo 200 cuando proceda.
- Si ves `For input string: "all"` al convertir Long, revisa controladores que reciban path variables/params numéricos y que no estén recibiendo el string literal `all`. Por ejemplo, asegúrate de tener rutas separadas `/resource/all` vs `/resource/{id}` o validar si `id` es numérico antes de convertir.

## Licencia

Añade aquí la licencia del proyecto si aplica (MIT, Apache 2.0, etc.).

---

Si quieres, puedo:

- Generar ejemplos concretos de `curl` para cada endpoint.
- Añadir un archivo `CONTRIBUTING.md` o plantillas de `ISSUE/PR`.
- Implementar las anotaciones `@JsonIgnoreProperties` necesarias en las entidades y ejecutar tests.

Dime cuál de estas tareas quieres que haga a continuación.

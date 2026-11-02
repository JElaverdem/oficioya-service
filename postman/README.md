# Pruebas con Postman y Swagger (oficioya-service)

## Swagger
1. Levantar el servicio en perfil dev (`.\mvnw spring-boot:run`, puerto 8081).
2. Abrir http://localhost:8081/swagger-ui.html
3. Obtener un token:
   - `POST /api/v1/auth/login` (cualquier correo/contraseña): devuelve un token con rol CONTRATANTE y un UUID aleatorio.
   - Para un token de TRABAJADOR, usar Postman: tras ejecutar cualquier request de la colección,
     copiar el valor de la variable de colección `tokenTrabajador`.
4. Botón **Authorize**, pegar el token (sin la palabra Bearer) y **Authorize**.

## Postman
1. Importar `OficioYa-Service.postman_collection.json` (Import > subir el archivo).
2. Sembrar los datos (cada vez que se vaya a correr la colección completa; ver abajo).
3. Clic derecho en la colección > **Run collection** > **Run**.
   Los tokens (trabajador, contratante, admin, expirado, alterado...) se generan solos con la clave de la variable `jwtSecret`.
   Si el servicio usa otra clave (`JWT_SECRET`), cambiar la variable `jwtSecret` de la colección.

## Sembrar datos
Con el contenedor de Postgres encendido:

    docker ps --format "{{.Names}}  {{.Ports}}"        # buscar el que expone 5434
    Get-Content .\postman\seed-postman.sql -Raw | docker exec -i NOMBRE_DEL_CONTENEDOR psql -U postgres -d oficioyaService

El script se puede repetir: borra y recrea solo las solicitudes de prueba (ids `aaaaaaaa-0000-...`).

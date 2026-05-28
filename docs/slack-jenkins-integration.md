# Integracion Slack + Jenkins - vg-ms-patrimonioservice

## 1. Descripcion

Se integro Jenkins con Slack para notificar automaticamente al equipo de desarrollo sobre el estado del pipeline del microservicio `vg-ms-patrimonioservice`. Cada build ejecuta compilacion, pruebas unitarias, empaquetado y pruebas de carga con JMeter, enviando el resultado a Slack.

## 2. Requisitos Previos

- Jenkins instalado (http://localhost:8090)
- Plugin **Slack Notification** instalado en Jenkins
- Bot de Slack configurado con token en el canal `#vg-ms-patrimonioservice`
- Repositorio GitLab conectado a Jenkins con credenciales

## 3. Configuracion del Pipeline

El archivo `Jenkinsfile` en la raiz del repositorio define el pipeline. La integracion con Slack se realiza mediante el paso `slackSend()` de Jenkins:

```groovy
post {
    success {
        slackSend(
            channel: '#vg-ms-patrimonioservice',
            color: '#36A64F',
            message: """
:white_check_mark: *Pipeline completado exitosamente*
*Proyecto:* vg-ms-patrimonioservice
*Build:* #${env.BUILD_NUMBER}
*Estado:* Compilacion, pruebas unitarias y empaquetado correctos
${env.JMETER_MSG}
"""
        )
    }
    failure {
        slackSend(
            channel: '#vg-ms-patrimonioservice',
            color: '#FF0000',
            message: """
:red_circle: *Pipeline FALLO*
*Proyecto:* vg-ms-patrimonioservice
*Build:* #${env.BUILD_NUMBER}
*Estado:* Error en alguna etapa del pipeline
${env.JMETER_MSG}
"""
        )
    }
}
```

## 4. Etapas del Pipeline

| Etapa | Descripcion |
|-------|-------------|
| **Build** | Compilacion del proyecto con Maven |
| **Test** | Ejecucion de pruebas unitarias |
| **Package** | Empaquetado del JAR |
| **Load Test** | Inicia el microservicio con H2, ejecuta JMeter (3 escenarios: Verde/Amarillo/Rojo), detiene el servicio |
| **Slack** | Envia notificacion con resumen del build y resultados de carga |

## 5. Mensajes de Slack

### Build exitoso

```
✅ Pipeline completado exitosamente
Proyecto: vg-ms-patrimonioservice
Build: #11
Estado: Compilacion, pruebas unitarias y empaquetado correctos
Pruebas de carga completadas | 9050 solicitudes en 00:00:59 | Promedio: 0ms | Max: 18ms | Throughput: 153.8/s | Errores: 0.00%
```

### Build fallido

```
🔴 Pipeline FALLO
Proyecto: vg-ms-patrimonioservice
Build: #12
Estado: Error en alguna etapa del pipeline
Pruebas de carga no ejecutadas
```

## 6. Captura de Pantalla

Para completar la tarea, adjuntar una captura de pantalla que muestre:

1. **Jenkins**: Abrir `http://localhost:8090/job/vg-ms-patrimonioservice/` y capturar la pantalla mostrando el pipeline y sus stages (Build, Test, Package, Load Test)
2. **Slack**: Capturar el mensaje recibido en el canal `#vg-ms-patrimonioservice` con la notificacion del build

## 7. Beneficios

- **Respuesta inmediata**: El equipo sabe al instante si un build fallo
- **Resultados de carga visibles**: Las metricas de JMeter se incluyen en la notificacion
- **Canal centralizado**: Todas las notificaciones de CI/CD en un solo canal de Slack
- **Integracion completa**: Desde el push a GitLab hasta la notificacion en Slack, todo automatizado

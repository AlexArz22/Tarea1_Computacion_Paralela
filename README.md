# Tarea 1 – Computación Paralela

Integrantes

-Claudio Cabello

-Alex Aravena

-Alvaro Catalan

-Leandro Chamorro

-David Martinez

-Martin Vasquez

## Requisitos

- **JDK 21** (recomendado)  
  > Si usas otro JDK, edita las líneas 13–14 de `pom.xml` para indicar tu versión (no recomendado).

- **Eclipse IDE**  

## Instalación

1. **Clonar el repositorio**  
   ```bash
   git clone https://github.com/AlexArz22/Tarea1_Computacion_Paralela.git
   
2. **Importar en Eclipse**
   
1. Ve a **File → Import… → Existing Projects into Workspace**  
2. Selecciona la carpeta `Tarea1_Computacion_Paralela`  
3. Asegúrate de que aparezca el proyecto `Tarea1_template` y haz clic en **Finish**

3. **Compilar con Maven**

1. Haz clic derecho en `Tarea1_template [Tarea1_Computacion_Paralela main]`  
2. Selecciona **Run As → Maven build…**  
3. En **Goals**, escribe:
   ```text
   clean install
   
4. Haz clic en Run

4. **Ejecución**

### Ejecutar el servidor
1. Dentro de `src/main/java`,y en el paquete del server ubica la clase `RunServer`  
2. Haz clic derecho → **Run As → Java Application**

### Ejecutar el cliente
1. Dentro de `src/main/java`,y en el paquete del cliente ubica la clase `RunClient`  
2. Haz clic derecho → **Run As → Java Application**

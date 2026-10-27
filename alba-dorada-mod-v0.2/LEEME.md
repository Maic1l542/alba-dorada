# Alba Dorada: Magia del Eclipse (versión 0.2)

Mod de fans para **Minecraft 1.20.1 con Forge**. Es un mod que hace Claude para Percy, capitán del Escuadrón Alba Dorada.

## Qué trae esta versión

- **Grimorio de Cinco Hojas en 3D**: lo recibes al entrar al mundo por primera vez. También está en la pestaña creativa "Alba Dorada".
- **Clic derecho**: lanza el hechizo elegido. **Agachado + clic derecho**: cambia de hechizo.
- **Barra de Maná y de Eclipse** arriba de la barra de objetos (solo se ve con el grimorio en la mano).
- **5 hechizos**: Espada de Luz, Lluvia de Destellos, Paso Dorado, Puño del Eclipse, Sello del Eclipse.
- **3 transformaciones con alas 3D** (se ven en tercera persona, con F5, y las ven los demás jugadores):
  - **Forma de Demonio**: alas oscuras con puntas doradas. Más daño, pero te quita vida poco a poco.
  - **Forma de Espíritu**: alas de luz dorada translúcida. Más velocidad y regeneración.
  - **Eclipse Total**: alas doradas y negras alternadas. Necesita la barra de Eclipse llena.
- Cada transformación aparece como un efecto con su icono en la esquina de la pantalla, con el tiempo que le queda.

Todavía **no** tiene: razas, estructuras, misiones, rangos que suban, ni el escuadrón de NPC. Eso viene en las siguientes versiones.

## Cómo conseguir el mod (sin instalar nada raro)

El mod se compila gratis en internet con GitHub. Solo necesitas una cuenta.

1. Crea una cuenta en https://github.com (gratis).
2. Arriba a la derecha pulsa **+** y luego **New repository**. Ponle de nombre `alba-dorada`, déjalo en **Public** y pulsa **Create repository**.
3. Pulsa el enlace **uploading an existing file**.
4. Arrastra **todo lo que está dentro de esta carpeta** (incluida la carpeta `.github`, `src` y los archivos sueltos) a la página y pulsa **Commit changes**.
   - Si la carpeta `.github` no se sube (a veces los navegadores ocultan las carpetas con punto), mira la sección "Si falta la carpeta .github" más abajo.
5. Entra a la pestaña **Actions**. Verás una tarea llamada "Compilar mod" que tarda unos 3 a 6 minutos. Cuando tenga una palomita verde, ábrela.
6. Baja hasta **Artifacts** y descarga `albadorada-mod`. Es un `.zip`: ábrelo y dentro está el archivo `.jar`, que es tu mod.
7. Si en vez de la palomita sale una **X roja**, entra a la tarea, copia el texto del error y mándaselo a Claude para que lo arregle.

**Si ya subiste la versión 0.1:** abre tu repositorio, borra los archivos viejos o sube encima los nuevos (arrastrando de nuevo todo) y vuelve a esperar la compilación.

## Cómo jugarlo con TLauncher

1. Abre TLauncher y en la lista de versiones elige **1.20.1 con Forge** (si no aparece, elige 1.20.1 y marca la opción de instalar Forge). Dale a instalar y entrar una vez, y cierra el juego.
2. Abre la carpeta de Minecraft: pulsa `Windows + R`, escribe `%appdata%\.minecraft` y Enter.
3. Entra a la carpeta `mods` (si no existe, créala) y pega el archivo `.jar`. Si tenías la versión 0.1, bórrala.
4. Abre TLauncher otra vez con la versión de Forge y crea un mundo.

## Si falta la carpeta .github

En tu repositorio pulsa **Add file → Create new file**, escribe como nombre `.github/workflows/build.yml` y pega dentro el contenido del archivo `build.yml` que viene en este proyecto. Luego **Commit changes**.

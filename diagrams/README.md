Class diagram (PlantUML) for the Hotelsimulator project

Files:
- class_diagram.puml  — PlantUML source for the full project class diagram

How to render to PNG or SVG locally (macOS / zsh):

1) If you have PlantUML installed (via Homebrew):

```bash
brew install plantuml
plantuml diagrams/class_diagram.puml
```

This produces diagrams/class_diagram.png (and/or .svg) in the same folder.

2) Using the PlantUML jar (no Homebrew):

```bash
# download plantuml.jar once
curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/latest/download/plantuml.jar
# render PNG
java -jar plantuml.jar diagrams/class_diagram.puml
```

3) Online: Copy the contents of `diagrams/class_diagram.puml` into https://www.planttext.com/ or https://plantuml.com/plantuml to render.

Notes:
- The diagram contains the major classes, interfaces, inheritance and primary associations (has-a / aggregation).
- If you want a more detailed diagram (showing all fields/methods for every class) I can expand the PlantUML to include them or generate separate diagrams per package.



# ChordalGraph

Java-Implementierung zur Bachelorarbeit
**„Ein Algorithmus zum Abzählen beschrifteter chordaler Graphen"**
(Universität zu Lübeck, Institut für Theoretische Informatik,
Betreuer: Prof. Dr. Maciej Liśkiewicz).

## Inhalt

| Datei | Beschreibung |
|---|---|
| `Main.java` | Berechnet die Anzahl `c(n)` der zusammenhängenden beschrifteten chordalen Graphen auf `n` Knoten für `n = 1, …, maxN`. Java-Portierung der Referenzimplementierung (siehe unten). |
| `LexBFS.java` | Beispielimplementierung von LexBFS (Partition Refinement) mit anschließender Prüfung, ob die Umkehrung der Reihenfolge eine perfekte Eliminationsordnung ist. Testet Zufallsgraphen für `n = 1, …, 30`. |

> Hinweis: Die Datei `Main.java` enthält die Klasse `Main` und muss daher
> auch genau so heißen.

## Quellen

Der Zählalgorithmus stammt von
U. Hébert-Johnson, D. Lokshtanov und E. Vigoda:
*Counting and Sampling Labeled Chordal Graphs in Polynomial Time*,
ESA 2023. Die Java-Version ist eine Portierung der Referenzimplementierung
der Autoren (C++): <https://github.com/uhebertj/chordal>.

Wie die Referenzimplementierung verwendet auch diese Portierung die
unoptimierte Variante des Algorithmus (ohne die Hilfsfunktion `h`), die
`O(n^8)` statt `O(n^7)` arithmetische Operationen benötigt.

## Ausführen

Voraussetzung: ein JDK (getestet mit Java 21; installiert wurde Eclipse
Temurin 25).

```bash
javac Main.java
java Main
```

Die Obergrenze `n` wird im Quelltext von `Main.java` in der Zeile
`int maxN = 35;` eingestellt. Bei größeren Werten steigen Laufzeit und
Speicherbedarf deutlich; gegebenenfalls mehr Heap-Speicher angeben,
z. B. `java -Xmx8g Main`.

Die Ausgabe hat die Form

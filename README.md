# Pulse – High-Performance HTTP Load Testing Tool

Pulse ist ein kleines CLI-Tool zum Lasttesten von HTTP-Endpunkten und Webservern.  
Es simuliert realistische Nutzungsszenarien mit frei definierbaren Anfragesequenzen und gleichzeitigen Clients.  
Ideal für Framework-Vergleiche, Performance-Benchmarks und Analyse von Anfälligkeiten für Rush Attacks

---

## Features
- **CLI:** Parametersteuerung per Argumente (`--sequence`, `--url`)
- **Parallelisierung:** Java Virtual Threads für das parallele Abfeuern der Requestkaskaden
- **Sequenzen** Frei definierbare Sequenzen
- **Request-Definition:** einfache Konfigurations-DSL über eine Textdatei `sequence.txt`

---

## Tech Stack
Java Virtual Threads

---

## Meine Idee

Mit dem Aufkommen neuerer Programmiersprachen und immer neuen Frameworks, stellt sich oft die Frage der Performance.
In der Welt der Webservice Backends kann man bspw. Endpunkte mit vielen gleichzeitigen Anfragen belasten und schauen, wie sich das auf das Antwortverhalten des Servers auswirkt.
Genau dafür ist Pulse geschrieben worden. Später ist mir klar geworden, dass damit je nach Parameter auch Rush Attacks simuliert werden können.

---

## Anwendung

Beim Starten der Anwendung sind die 2 folgenden Argumente mitzugeben
```bash
--sequence={Dateipfad zu einer .txt mit der Requestsequence}
--url={Url mit Endpunkt der belastet werden soll}

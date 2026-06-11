# Barbell Tracker — User Requirements Dokument

> **Version:** v0.1  
> **Autor:** ChrisOTM  
> **Datum:** 2026-06-11  
> **Plattform:** Android (nativ, Jetpack Compose)  
> **Ziel-SDK:** 36 (Android 16)

---

## 1. Zielgruppe & Problem

### Zielgruppe
Menschen, die regelmäßig ins Fitnessstudio gehen und **Langhanteltraining (Barbell Training)** absolvieren — Anfänger wie Fortgeschrittene, die nach einem strukturierten Trainingsplan trainieren wollen.

### Problem
Viele Trainings laufen unstrukturiert ab. Sätze und Wiederholungen werden improvisiert, Pausenzeiten werden nicht eingehalten, und die Progression (Steigerung des Gewichts) erfolgt unsystematisch. Das führt zu suboptimalen Trainingsergebnissen oder Stagnation.

### Lösung
Eine native Android-App, die einen strukturierten Trainingsplan vorgibt, die Trainingseinheit Schritt für Schritt steuert, Pausenzeiten automatisch berechnet, das Absolvieren protokolliert und eine systematische Gewichts-Progression vorschlägt.

---

## 2. User Stories

### Prioritäten
- **★ MUST** — zwingend für MVP
- **◈ SHOULD** — wichtig, aber nicht blockierend
- **○ COULD** — Nice to Have

---

### EPIC 1: Trainingsplan verwalten

#### US-1.1: Mitgelieferte Übungen ansehen ★ MUST

**Als** Nutzer  
**möchte ich** eine mitgelieferte Liste von Langhantel-Übungen sehen  
**um** zu wissen, welche Übungen ich in meinen Trainingsplan aufnehmen kann.

**Akzeptanzkriterien:**
- Die App enthält eine vordefinierte Übungsbibliothek mit mindestens folgenden Übungen:
  - Langhantel-Kniebeuge (Barbell Back Squat)
  - Bankdrücken (Barbell Bench Press)
  - Kreuzheben (Barbell Deadlift)
  - Langhantel-Rudern (Barbell Row)
  - Schulterdrücken (Military Press / Overhead Press)
  - Frontkniebeuge (Barbell Front Squat)
  - Langhantel-Ausfallschritte (Barbell Lunges)
  - Good Mornings
  - Rumänisches Kreuzheben (Romanian Deadlift)
  - Kurzhantel-Rudern einarmig (Dumbbell Row) — als einzige Hantel-Übung
  - Klimmzüge (Pull-ups) — als Körpergewichts-Übung
- Jede Übung enthält:
  - Name
  - Ziel-Muskelgruppe(n)
  - Kurze Beschreibung der Ausführung (1–3 Sätze)
- Die Liste ist in der App scrollbar und durchsuchbar.

#### US-1.2: Eigene Übung erstellen ★ MUST

**Als** Nutzer  
**möchte ich** eigene Übungen mit Namen und Beschreibung anlegen  
**um** Übungen zu nutzen, die nicht in der Standard-Liste enthalten sind.

**Akzeptanzkriterien:**
- Neues Übung anlegen mit: Name (Pflicht), Beschreibung (optional), Muskelgruppe (optional)
- Die eigene Übung erscheint in der Übungsbibliothek und kann wie alle anderen in Plänen verwendet werden
- Eigene Übungen sind editierbar und löschbar

#### US-1.3: Trainingsplan aus Vorlage erstellen ★ MUST

**Als** Nutzer  
**möchte ich** aus einer mitgelieferten Vorlage einen Trainingsplan erstellen  
**um** ohne manuelle Konfiguration sofort starten zu können.

**Akzeptanzkriterien:**
- Vorlage "GK 5×5" (Ganzkörper, Stronglifts-ähnlich):
  - **Workout A:** Kniebeuge 5×5, Bankdrücken 5×5, Langhantel-Rudern 5×5
  - **Workout B:** Kniebeuge 5×5, Schulterdrücken 5×5, Kreuzheben 1×5
- Vorlage "GK 3×8":
  - **Workout A:** Kniebeuge 3×8, Bankdrücken 3×8, Langhantel-Rudern 3×8
  - **Workout B:** Kniebeuge 3×8, Schulterdrücken 3×8, Kreuzheben 1×8
- Die Vorlage wird beim Erstellen kopiert (keine Änderung des Originals)
- Pausenzeiten sind standardmäßig voreingestellt (siehe US-2.4)

#### US-1.4: Eigenen Trainingsplan zusammenstellen ★ MUST

**Als** Nutzer  
**möchte ich** aus der Übungsbibliothek einen eigenen Trainingsplan zusammenstellen  
**um** mein Training genau an meine Bedürfnisse anzupassen.

**Akzeptanzkriterien:**
- Neuen Plan anlegen mit Namen
- Übungen aus der Bibliothek hinzufügen
- Pro Übung einstellbar:
  - Anzahl Sätze
  - Anzahl Wiederholungen
  - Zielgewicht (optional, kann auch erst im Training gesetzt werden)
  - Pausenzeit (Standard-Wert oder eigener Wert)
- Übungen im Plan sortierbar (drag & drop oder per Button)
- Plan editierbar und löschbar
- Mehrere Pläne gleichzeitig verwalten

#### US-1.5: Trainingsplan-Workouts konfigurieren ★ MUST

**Als** Nutzer  
**möchte ich** einem Trainingsplan mehrere Workouts (A, B, C, …) zuweisen  
**um** zwischen verschiedenen Trainingstagen zu unterscheiden.

**Akzeptanzkriterien:**
- Pro Plan können mehrere Workouts angelegt werden (z.B. Workout A, Workout B)
- Jedes Workout hat eine eigene Übungsliste
- Beim Starten eines Trainings wählt der Nutzer, welches Workout er absolviert

---

### EPIC 2: Training ausführen

#### US-2.1: Workout starten ★ MUST

**Als** Nutzer  
**möchte ich** ein Workout aus meinem Trainingsplan starten  
**um** mit dem Training zu beginnen.

**Akzeptanzkriterien:**
- Der Nutzer wählt einen Plan → ein Workout → "Starten"
- Alle Übungen des Workouts werden in der festgelegten Reihenfolge geladen
- Vor Beginn wird eine Übersicht angezeigt: welche Übungen, Sätze, Gewichte
- Das Workout kann jederzeit pausiert werden (US-2.6)

#### US-2.2: Aktuelle Übung mit Sätzen und Wiederholungen anzeigen ★ MUST

**Als** Nutzer  
**möchte ich** während des Trainings sehen, in welcher Übung und in welchem Satz ich bin  
**um** den Überblick zu behalten.

**Akzeptanzkriterien:**
- Die App zeigt deutlich an:
  - Aktuelle Übung (Name + Gewicht)
  - Satz X von Y (z.B. "Satz 2 von 5")
  - Vorgegebene Wiederholungen (z.B. "8 Wdh.")
- Fortschrittsanzeige: visueller Balken oder Punkte für Sätze

#### US-2.3: Satz absolvieren oder scheitern ★ MUST

**Als** Nutzer  
**möchte ich** nach jedem Satz angeben, ob ich die vorgegebenen Wiederholungen geschafft habe oder nicht  
**um** die Progression korrekt zu protokollieren.

**Akzeptanzkriterien:**
- Nach Abschluss eines Satzes: zwei Buttons
  - ✅ **Geschafft** — alle Wdh. mit sauberer Ausführung absolviert
  - ❌ **Nicht geschafft** — Wdh. nicht vollständig oder mit schlechter Form
- Die tatsächlich geschafften Wiederholungen sind editierbar (z.B. 8 von 10)
- Nach der Eingabe startet automatisch der Pausen-Timer

#### US-2.4: Pausen-Timer ★ MUST

**Als** Nutzer  
**möchte ich** nach jedem Satz einen automatischen Timer haben  
**um** die Pausenzeit einzuhalten.

**Akzeptanzkriterien:**
- Standard-Pausenzeiten pro Übung (aus dem Trainingsplan)
- Nach dem Abhaken eines Satzes startet der Timer automatisch
- Der Timer zeigt die verbleibende Zeit groß an
- Defaults (wenn kein eigener Wert gesetzt):
  - 5×5 Übungen: 3:00 Minuten
  - 3×8 / 3×10 Übungen: 1:30 Minuten
  - Kreuzheben (1×5): 3:00 Minuten
- Der Timer kann jederzeit pausiert werden (US-2.6)
- Der Timer läuft im Vordergrund; bei Bildschirm-Aus: Countdown läuft weiter (Foreground Service) — optional

#### US-2.5: Nächsten Satz / nächste Übung starten ★ MUST

**Als** Nutzer  
**möchte ich** nach Ablauf des Timers bestätigen, dass ich bereit für den nächsten Satz bin  
**um** das Tempo selbst zu bestimmen.

**Akzeptanzkriterien:**
- Nach Timer-Ablauf: Button "Nächster Satz"
- Erst nach Bestätigung wird der nächste Satz angezeigt
- Bei letztem Satz einer Übung: Button "Nächste Übung"
- Bei letzter Übung: Button "Workout beenden"

#### US-2.6: Workout pausieren ★ MUST

**Als** Nutzer  
**möchte ich** das Workout jederzeit pausieren können  
**um** bei Unterbrechungen (Gespräch, Telefon, Trinken) nicht den Timer zu verlieren.

**Akzeptanzkriterien:**
- Pause-Button während des gesamten Workouts
- Timer und Workout werden angehalten
- Nach Rückkehr: Button "Fortsetzen"
- Pausierte Zeit wird nicht protokolliert

#### US-2.7: Workout vorzeitig beenden ★ MUST

**Als** Nutzer  
**möchte ich** ein Workout vorzeitig beenden können  
**um** abzubrechen, wenn ich keine Zeit habe oder mich unwohl fühle.

**Akzeptanzkriterien:**
- "Workout beenden"-Button während des Workouts
- Bestätigungsdialog: "Wirklich beenden? Bereits absolvierte Sätze werden gespeichert."
- Bereits geschaffte Sätze werden im Trainingstagebuch gespeichert

---

### EPIC 3: Gewichts-Progression

#### US-3.1: Gewicht pro Übung setzen ★ MUST

**Als** Nutzer  
**möchte ich** für jede Übung in meinem Plan ein Zielgewicht angeben  
**um** zu wissen, wie viel Gewicht ich auflegen muss.

**Akzeptanzkriterien:**
- Gewichtseingabe in kg (ganze oder halbe Schritte: z.B. 52.5 kg)
- Gewicht kann sowohl vor dem Workout (im Plan) als auch während des Workouts geändert werden
- Das zuletzt verwendete Gewicht wird als Default für die nächste Einheit vorgeschlagen

#### US-3.2: Automatische Gewichts-Progression ★ MUST

**Als** Nutzer  
**möchte ich** dass die App mir nach einem erfolgreichen Workout eine Gewichtserhöhung vorschlägt  
**um** systematisch stärker zu werden.

**Akzeptanzkriterien:**
- Wenn alle Sätze einer Übung in der Einheit geschafft wurden:
  - Vorschlag: **+2,5 kg** für die nächste Einheit
  - Wert ist manuell editierbar (z.B. +5 kg, +1,25 kg, +0 kg)
- Wenn ein oder mehrere Sätze nicht geschafft wurden:
  - Vorschlag: **gleiches Gewicht** beibehalten
  - Wert ist manuell editierbar
- Die Entscheidung („Progression übernehmen / anpassen") wird vor dem nächsten Workout getroffen

#### US-3.3: Gewichtsanpassung während des Workouts ◈ SHOULD

**Als** Nutzer  
**möchte ich** das Gewicht während eines laufenden Workouts anpassen können  
**um** bei Bedarf (z.B. schlechter Tag) das Gewicht zu reduzieren.

**Akzeptanzkriterien:**
- Während der Übungsansicht: Gewicht editierbar
- Änderung gilt für den aktuellen und alle folgenden Sätze dieser Übung
- Die Änderung wird im Trainingstagebuch protokolliert

---

### EPIC 4: Trainingstagebuch

#### US-4.1: Abgeschlossenes Workout speichern ★ MUST

**Als** Nutzer  
**möchte ich** dass ein abgeschlossenes Workout automatisch gespeichert wird  
**um** meine Trainingshistorie zu haben.

**Akzeptanzkriterien:**
- Nach erfolgreichem Abschluss (oder vorzeitigem Beenden) wird gespeichert:
  - Datum & Uhrzeit (Start + Ende)
  - Trainingsplan + Workout-Name
  - Pro Übung: Gewicht, Sätze, Wiederholungen (geplant vs. tatsächlich)
  - Ob die Sätze geschafft wurden
  - Tatsächliche Pausenzeiten
- Die Speicherung erfolgt lokal auf dem Gerät

#### US-4.2: Trainingshistorie ansehen ★ MUST

**Als** Nutzer  
**möchte ich** meine abgeschlossenen Workouts in einer Historie ansehen  
**um** meinen Fortschritt zu verfolgen.

**Akzeptanzkriterien:**
- Chronologische Ansicht (neueste zuerst)
- Pro Eintrag: Datum, Plan, Übungen mit Gewicht, Sätze, Wdh.
- Filter nach Trainingsplan möglich
- Suche nach Datum möglich

#### US-4.3: Fortschritt pro Übung sehen ◈ SHOULD

**Als** Nutzer  
**möchte ich** pro Übung sehen, wie sich mein Gewicht über die Zeit entwickelt hat  
**um** meine Progression zu visualisieren.

**Akzeptanzkriterien:**
- Übung auswählen → einfache Diagrammansicht (Gewicht über Zeit)
- Alle absolvierten Einheiten dieser Übung als Punkte / Linie
- Entwicklung der Wiederholungen optional

---

### EPIC 5: Allgemein & Erweiterbarkeit

#### US-5.1: App komplett offline nutzbar ★ MUST

**Als** Nutzer  
**möchte ich** die App vollständig ohne Internetverbindung nutzen können  
**um** auch im Kellerstudio oder bei schlechtem Empfang trainieren zu können.

**Akzeptanzkriterien:**
- Alle Funktionen sind ohne Internet verfügbar
- Kein Login, keine Registrierung, keine Cloud-Synchronisation
- Keine Werbung, kein Tracking

#### US-5.2: Erweiterbarkeit der Architektur ★ MUST

**Als** Entwickler  
**möchte ich** dass die App so architekturiert ist, dass neue Features einfach hinzugefügt werden können  
**um** die App später um Online-Funktionalität, mehrere Benutzer oder Wear-OS erweitern zu können.

**Akzeptanzkriterien:**
- Klare Schichten: UI (Compose) → ViewModel → Repository → DAO → Room-Datenbank
- Repository-Pattern: später austauschbar gegen Remote-Repository (Online-Feature)
- Dependency Injection (Hilt oder Koin)
- Keine Geschäftslogik in UI-Komponenten
- Datenbank-Migrationen via Room (schema-versioniert)

#### US-5.3: Material Design 3 UI ◈ SHOULD

**Als** Nutzer  
**möchte ich** eine moderne, saubere Benutzeroberfläche  
**um** mich in der App zurechtzufinden und motiviert zu trainieren.

**Akzeptanzkriterien:**
- Material Design 3 (Material You) Design-Sprache
- Dynamische Farben (Android 12+) optional
- Dark Mode Unterstützung
- Große, gut lesbare Schrift (während des Trainings aus Distanz lesbar)
- Haptisches Feedback bei Satz-Abschluss

---

## 3. Nicht-Ziele (Out of Scope für MVP)

- ❌ **Kein Wear OS / Smartwatch** — nur Smartphone
- ❌ **Kein Kalender / Wochenplan** — Training wird manuell gestartet
- ❌ **Kein Scheibenrechner** — kein automatisches Berechnen der aufzulegenden Scheiben
- ❌ **Kein Social Sharing / Community**
- ❌ **Kein Video-Coaching / Übungs-Videos**
- ❌ **Kein Ernährungs-Tracker**
- ❌ **Kein Mehrbenutzer-Modus** — nur ein lokaler User
- ❌ **Keine Cloud / Backup / Sync** (vorerst — Architektur aber vorbereitet)
- ❌ **Kein Import/Export von Trainingsplänen** (Nice to Have für später)

---

## 4. Technische Randbedingungen

| Aspekt | Vorgabe |
|--------|---------|
| **Plattform** | Android (kein iOS / Web) |
| **UI-Framework** | Jetpack Compose (kein XML) |
| **Ziel-SDK (targetSdk)** | 36 (Android 16) |
| **Min-SDK (minSdk)** | 26 (Android 8) — ca. 95%+ Geräteabdeckung |
| **Sprache** | Kotlin |
| **Datenbank** | Room (SQLite, lokal) |
| **DI** | Hilt oder Koin |
| **Architektur** | MVVM + Repository Pattern |
| **Offline** | Vollständig offline, kein Internet benötigt |
| **Erweiterbarkeit** | Architektur muss später Online-Feature, Wear-OS oder Multi-User ermöglichen |
| **UI-Design** | Material Design 3, dynamische Farben, Dark Mode |
| **Build-System** | Gradle (Version aktuell, AGP aktuell) |

---

## 5. Offene Punkte / Entscheidungen

- [ ] **Timer im Hintergrund:** Soll der Timer als Foreground Service laufen (Countdown läuft auch bei Bildschirm-Aus) oder nur Vordergrund?
- [ ] **Scheibenrechner** — bewusst zurückgestellt, könnte später als COULD-Feature kommen
- [ ] **KI-basierte Gewichts-Progression** — könnte später adaptiv vorschlagen (z.B. basierend auf letzten 3 Einheiten)
- [ ] **Export der Trainingshistorie** (CSV/PDF) — späteres Feature

---

## 6. Glossar

| Begriff | Bedeutung |
|---------|-----------|
| **GK** | Ganzkörpertraining |
| **5×5** | 5 Sätze à 5 Wiederholungen |
| **3×8** | 3 Sätze à 8 Wiederholungen |
| **Rep / Wdh.** | Wiederholung |
| **Set / Satz** | Eine Gruppe von Wiederholungen |
| **Progression** | Systematische Gewichtssteigerung über Zeit |
| **RM** | Repetition Maximum (maximales Gewicht für X Wdh.) |
| **Room** | Android-eigene Abstraktionsschicht über SQLite (Google empfohlen) |
| **Compose** | Jetpack Compose — modernes, deklaratives Android UI-Toolkit |
| **MVVM** | Model-View-ViewModel — bewährtes Architekturmuster für Android |
| **Repository-Pattern** | Trennung der Datenquellen (lokal/remote) hinter einer einheitlichen Schnittstelle |

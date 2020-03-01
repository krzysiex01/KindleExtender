# KindleExtender

KindleExtender to aplikacja rozszerzająca dla funkcji Vocabulary Builder używaniej przez czytniki Kindle’a.

## Wprowadzenie:
Podczas czytania książek w obcym języku urządzenie gromadzi dane na temat sprawdzanych w słowniku słówek. Funkcja Vocabulary Builder pozwala na późniejsze przeglądanie tych słów i oznaczanie ich jako „nauczone” lub „w trakcie nauki”. Mimo jej przydatności posiada wiele ograniczeń:
- limit przechowywanych słów
- format (i lokalizacja) przechowywania nieprzestępna dla użytkownika (mobilna baza danych (*.db))
- bardzo ograniczone (lub całkowity brak) możliwości edycji, usuwania, sortowania
- liczne duplikaty (np. do, did, done, does – jako oddzielne wpisy w bazie lub np. ‘go’ i go – traktowane jako inne słowa)
- jedna baza dla wszystkich języków

## Opis:
Aplikacja ma ułatwić użytkownikom zarządzanie, edycje oraz konwersje danych zapisywanych przez czytniki Kindla. 

## Konspekt projektu:
- otwieranie i wyświetlanie zgromadzonych danych
- sortowanie słów po liczbie wystąpień (częstości sprawdzania)
- konwersja danych do formatu CSV
- możliwość edycji zgromadzonych danych
- możliwość szybkego usuwania wpisów z bazy danych
- funkcja czyszczenia (usuwanie przypadkowych wpisów i duplikatów)
- filtrowanie ze względu na wybrany język
- tworzenie kopii zapasowej
- wyświetlanie statystyk dla zgromadzonych danych
- możliwość sprawdzenia tłumaczenia dla wybranego słowa

## Szczegóły techniczne
* Aplikacja napisana w języku Java z wykorzystaniem JavaFX.
* Tłumaczenie zgromadzonych słówek odbywa się za pomocą usługi Microsoft Translator - wywołanie interfejsu API REST.
* Tworzenie i konwersja pilków csv z użyciem Apache Commons CSV
* Wszelkie operacje na danych uzyskanych z urządzenia przetwarzane jako transakcje bazy danych wykorzystując bibliotekę SQLite JDBC.

### Wykorzystane biblioteki
* SQLite JDBC
* Apache Commons CSV
* OkHttp
* JSON (org.json)

## Uwagi:
Przykładowe dane znajdują się w folderze SampleData

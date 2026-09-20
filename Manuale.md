# SimCity Lite — Manuale di gioco

## Obiettivo del gioco

SimCity Lite è un piccolo simulatore gestionale in cui bisogna far crescere una città mantenendo sotto controllo **budget, popolazione, economia, inquinamento, felicità, energia e posti di lavoro**.

La città è costruita su una griglia **20 × 20**. Ogni pressione di **Next Turn** fa avanzare la simulazione di un turno e aggiorna edifici, statistiche, eventi e budget.

La partita inizia con **2500 €**.

## Costruzione della città

La prima strada può essere costruita liberamente. Tutte le strade successive devono essere adiacenti a una strada già esistente.

Gli altri edifici devono invece essere costruiti accanto a una strada. Alcune costruzioni diventano disponibili solo più avanti nella partita oppure richiedono particolari condizioni, come un numero sufficiente di lavoratori.

Costi base principali:

| Costruzione | Costo base |
| --- | ---: |
| Strada | 50 € |
| Zona residenziale | 200 € |
| Zona commerciale | 300 € |
| Zona industriale | 600 € |
| Parco | 150 € |
| Centrale elettrica | 800 € |
| Banca | 800 € |
| Impresa edile | 1000 € |
| Stazione di polizia | 1200 € |

Le politiche cittadine possono modificare alcuni prezzi.

Le strade non possono essere demolite. Per demolire le altre costruzioni serve almeno un'**impresa edile alimentata**; il numero di demolizioni disponibili dipende dalle imprese edili presenti e si rinnova periodicamente.

## Popolazione, lavoro ed economia

Le zone residenziali ospitano gli abitanti della città. Se sono alimentate, la popolazione cresce; se rimangono senza corrente, la popolazione diminuisce e la zona può infine scomparire.

Le zone commerciali e industriali offrono posti di lavoro e producono denaro. Per costruirle deve essere disponibile un numero sufficiente di cittadini senza lavoro.

Il valore **Economy** indica quanto la città sta producendo economicamente in quel momento. Non rappresenta il denaro già disponibile: quello è il **Budget**. L'economia deriva soprattutto dalla produzione delle zone commerciali e industriali alimentate, può essere modificata dalla policy attiva e può essere ridotta da effetti negativi come le attività criminali.

La produzione dei singoli edifici economici cresce gradualmente nel tempo finché rimangono alimentati. Le industrie possono produrre molto più dei commerciali, ma richiedono più lavoratori, consumano più energia, generano più inquinamento e hanno costi di manutenzione maggiori.

A ogni turno il gioco usa quindi, in modo semplificato, questa relazione:

**variazione del Budget = Economy - costi di manutenzione**

A questo risultato possono poi aggiungersi altri costi o effetti, per esempio eventi, assicurazioni o rimborso dei prestiti. Se un edificio commerciale o industriale non è alimentato, non produce economia.

## Energia

Le centrali elettriche alimentano le costruzioni vicine entro la propria area di copertura.

Ogni edificio consuma una quantità diversa di energia. Se una centrale non riesce ad alimentare tutto, alcune costruzioni vengono scollegate; quando torna disponibile energia sufficiente, vengono ricollegate automaticamente.

Gli edifici senza energia sono indicati nella griglia con il simbolo del fulmine.

## Manutenzione

Oltre al costo iniziale di costruzione, alcune strutture hanno un **costo di manutenzione per turno**, che viene sottratto dal guadagno della città.

Per esempio:
- strada: 2 € per turno;
- zona residenziale: 5 €;
- parco: 10 €;
- zona commerciale: 30 €;
- banca: 60 €;
- impresa edile: 40 €;
- zona industriale: 200 €;
- stazione di polizia: 120 €.

Per questo motivo non basta costruire molto: bisogna assicurarsi che l'economia riesca a sostenere i costi della città.

Se la città rimane per molti turni in una situazione economica critica, può andare incontro al fallimento.

## Felicità e inquinamento

Le costruzioni influenzano in modo diverso la città.

I parchi aumentano la felicità e riducono l'inquinamento. Le zone commerciali aumentano moderatamente entrambi, mentre le industrie producono molto inquinamento e riducono la felicità.

In alcune celle che diventano inutilizzabili può comparire automaticamente dell'**erba**. L'erba riduce leggermente l'inquinamento e può essere rimossa.

## Politiche cittadine

La città può utilizzare tre politiche:

- **Standard Policy**: comportamento normale;
- **Environmental Policy**: favorisce la riduzione dell'inquinamento, ma riduce anche l'economia;
- **Industrial Policy**: favorisce l'economia, aumentando però anche l'inquinamento.

La politica non può essere cambiata continuamente: dopo un cambio devono passare **12 turni** prima di poterla modificare di nuovo.

## Banche e prestiti

Le banche diventano disponibili più avanti nella partita.

Una banca alimentata permette di chiedere un prestito. Più banche alimentate sono presenti, maggiore può essere l'importo massimo richiesto.

Il prestito diventa da restituire dopo 3 turni. L'importo da restituire è pari a **1,5 volte** la somma richiesta, quindi va usato con attenzione.

## Assicurazione contro lo tsunami

Se è presente almeno una banca, è possibile acquistare un'**assicurazione contro lo tsunami**.

Il prezzo dipende dalle costruzioni presenti nelle zone della mappa che possono essere raggiunte dallo tsunami e dal loro tipo. Ogni banca applica uno **sconto del 5%** sul costo complessivo, fino a uno sconto massimo del **30%**.

L'assicurazione copre soltanto gli edifici presenti nel momento in cui viene applicata. Se in seguito vengono costruiti nuovi edifici nella zona a rischio, è necessario premere nuovamente il pulsante dell'assicurazione per estendere la copertura soltanto a quelle nuove costruzioni.

Quando uno tsunami distrugge edifici assicurati, questi vengono ricostruiti gradualmente dopo la fine dell'animazione, mantenendo lo stato che avevano prima della distruzione. Durante la ricostruzione non è possibile costruire nella zona interessata.

## Attività criminali e polizia

Con la crescita dell'economia possono comparire casualmente delle **attività criminali**. Queste riducono l'economia e la felicità della città.

Una stazione di polizia alimentata può eliminare un'attività criminale quando questa è rimasta attiva per **almeno 3 turni**. Le stazioni di polizia non possono effettuare rimozioni continuamente: tra un intervento e il successivo deve trascorrere del tempo.

## Eventi casuali

Durante la partita può essere attivo un solo evento casuale alla volta.

Gli eventi principali sono:

- **Attacco hacker**: sospende temporaneamente una centrale elettrica;
- **Crisi energetica**: aumenta temporaneamente i costi legati agli edifici alimentati;
- **Boom economico**: aumenta temporaneamente la produzione di zone commerciali e industriali;
- **Incendio**: può propagarsi tra costruzioni vicine e distruggerle;
- **Tsunami**: arriva da uno dei quattro lati della mappa e colpisce le prime quattro righe o colonne.

Lo tsunami distrugge anche i parchi. Non distrugge invece strade ed erba. Le costruzioni assicurate possono essere ricostruite dopo l'evento.

## Salvataggio

La partita può essere salvata con **Save Game** e caricata successivamente dalla schermata iniziale.

Il salvataggio conserva le principali informazioni della città, comprese le costruzioni, il budget, il turno raggiunto, la politica e lo stato degli edifici.

## Avvio del gioco

Sono necessari **JDK 17** e **Apache Maven**.

Dalla cartella che contiene il file `pom.xml`:

```bash
mvn clean javafx:run
```

Per eseguire i test:

```bash
mvn test
```

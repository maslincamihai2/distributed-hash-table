package org.example;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Main {
    public static int id;
    public static int portServerLocal;
    public static String ipSuccesor;
    public static int portSuccesor;
    public static int numarPeers = 3;
    public static HashMap<String, String> dictionar = new HashMap<>();

    public static void main(String[] args) {

        // daca nu sunt date argumentele necesare, programul se opreste imediat
        if (args.length != 4) {
            System.exit(-1);
        }

        // citire argumente
        id = Integer.parseInt(args[0]);
        portServerLocal = Integer.parseInt(args[1]);
        ipSuccesor = args[2];
        portSuccesor = Integer.parseInt(args[3]);

        // initializeaza date pentru test
        switch (id) {
            case 0:
                dictionar.put("paine", "aliment");
                break;
            case 1:
                dictionar.put("albastru", "culoare");
                break;
            case 2:
                dictionar.put("telefon", "dispozitiv");
        }

        // server
        Thread serverThread = new ServerThread();
        serverThread.start();

        // client
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String cheie = "";
        do {
            System.out.print("> ");

            // se citeste cheia de la tastatura
            try {
                cheie = consoleReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // calculeaza hash-ul cheii
            int hash = functieHash(cheie);
            System.out.println("Cheie: " + cheie + ", hash: " + hash);

            String valoare = null;
            // se compara hash-ul cheii cu id-ul propriu
            // daca hash <= id atunci valoarea cheii ar putea fi pe acest nod
            if (hash <= id) {
                valoare = dictionar.get(cheie);

                // daca se gaseste valoarea local
                if (valoare != null) {
                    System.out.println("Valoarea cheii " + cheie + " a fost gasita in accest nod: " + valoare);
                } else {
                    cautaInSuccesor(cheie, String.valueOf(id));
                }

                // daca hash > id atunci valoarea nu poate fi gasita in acest nod
            } else {
                cautaInSuccesor(cheie, String.valueOf(id));
            }
        } while (!cheie.equals("bye"));
    }

    public static int functieHash(String cheie) {
        // in acest exemplu hash-ul se obtine prin adunarea valorilor caracterelor din cuvantul cheie
        int hash = 0;
        for (int i = 0; i < cheie.length(); i++) {
            hash += cheie.charAt(i);
        }
        return hash % numarPeers;
    }

    public static void cautaInSuccesor(String cheie, String idQueryingNode) {
        System.out.println("Se cauta cheia " + cheie + " in alt nod");

        // daca e mai mare atunci pune id propriu in mesaj
        // pentru a sti unde trebuie sa ajunga valoarea cheii care este gasita in alt nod
        String mesaj = "cauta " + cheie + " " + idQueryingNode;

        trimiteMesaj(ipSuccesor, portSuccesor, mesaj);
    }

    /**
     * Metoda pentru trimiterea unui mesaj catre un alt peer cand se cunoaste ip-ul si portul
     *
     * @param ipDestinatar   este ip-ul acelui peer caruia i se trimite mesajul
     * @param portDestinatar este portul pe care asculta destinatarul cererile de la alti peers
     * @param mesaj          este continutul mesajului
     * @return raspunsul primit in urma mesajului sau sir gol ("") in caz de eroare
     */
    public static String trimiteMesaj(String ipDestinatar, int portDestinatar, String mesaj) {
        try {
            // deschide socket pentru comunicarea cu succesorul
            Socket socketClient = new Socket(ipDestinatar, portDestinatar);

            // fluxuri pentru citire si scriere
            BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

            out.println(mesaj);
            String raspuns = in.readLine();

            System.out.println("Raspuns pentru mesajul " + mesaj + " catre " + ipDestinatar + ":" + portDestinatar);
            System.out.println(raspuns);
            System.out.println("-----------------------------------------------");
            return raspuns;
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("eroare trimitere mesaj " + mesaj + " catre " + ipDestinatar + ":" + portDestinatar);
            System.out.println("************************************************");
            return "";
        }
    }
}

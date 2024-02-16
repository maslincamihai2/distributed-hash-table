package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.example.Main.*;

public class ServerThread extends Thread {

    @Override
    public void run() {

        // declarare variabile
        ServerSocket socketServer = null;

        // initializare
        try {
            socketServer = new ServerSocket(Main.portServerLocal);
        } catch (Exception e) {
            // portul pe care se porneste serverul local poate fi folosit de alt proces
            e.printStackTrace();
            System.out.println("eroare pornire server");
        }

        while (Thread.interrupted() == false) {
            try {
                // asteapta conexiune noua initializata de un client
                Socket socketClient = socketServer.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
                PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

                // citire sir de caractere primit
                String sirCaractere = in.readLine();

                System.out.println("sir de caractere primit:");
                System.out.println(sirCaractere);
                System.out.println("-----------------------------------------------");

                //se imparte sirul de caractere primit in subsiruri
                //primul subsir pana la spatiu reprezinta un mesaj
                String[] subsiruri = sirCaractere.split(" ");
                String comanda = subsiruri[0];
                String idQueryingNode = subsiruri[2];

                switch (comanda) {
                    case "cauta":
                        String cheie = subsiruri[1];

                        // calculeaza hash-ul cheii
                        int hash = functieHash(cheie);
                        System.out.println("Cheie: " + cheie + ", hash: " + hash);

                        String valoare = null;
                        // se compara hash-ul cheii cu id-ul propriu
                        // daca hash <= id atunci valoarea cheii ar putea fi pe acest nod
                        if (hash <= Main.id) {
                            valoare = dictionar.get(cheie);

                            // daca se gaseste valoarea local
                            if (valoare != null) {
                                System.out.println("Valoarea cheii " + cheie + " a fost gasita in accest nod: " + valoare);

                                String mesaj = "redirectioneaza " + valoare + " " + idQueryingNode + " " + Main.id;

                                trimiteMesaj(Main.ipSuccesor, Main.portSuccesor, mesaj);

                            } else {
                                cautaInSuccesor(cheie, idQueryingNode);
                            }

                            // daca hash > id atunci valoarea nu poate fi gasita in acest nod
                        } else {
                            cautaInSuccesor(cheie, idQueryingNode);
                        }
                        break;
                    case "redirectioneaza":
                        if (idQueryingNode.equals(String.valueOf(Main.id))) {

                            String valoareGasita = subsiruri[1];
                            String idNodValoareGasita = subsiruri[3];

                            System.out.println("Valoare: " + valoareGasita + ", nod unde am gasit valoarea: " + idNodValoareGasita);
                        } else {
                            System.out.println("Valoarea gasita intr-un nod predecesor este redirectionata");
                            trimiteMesaj(Main.ipSuccesor, Main.portSuccesor, sirCaractere);
                        }
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("IOException Server Thread");
            }
        }
    }
}

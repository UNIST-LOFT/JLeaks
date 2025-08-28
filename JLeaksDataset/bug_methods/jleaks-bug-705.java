    public static void writeInfoFile(JFrame paFrame, Daten daten, DatenFilm film) {
        String titel = film.arr[DatenFilm.FILM_TITEL_NR];
        titel = GuiFunktionen.replaceLeerDateiname(titel);
        String pfad = "";
        ListePset lp = Daten.listePset.getListeSpeichern();
        if (lp.size() > 0) {
            DatenPset p = lp.get(0);
            pfad = p.getZielPfad();
        }
        if (pfad.isEmpty()) {
            pfad = GuiFunktionen.getStandardDownloadPath();
        }
        if (titel.isEmpty()) {
            titel = film.arr[DatenFilm.FILM_SENDER_NR].replace(" ", "-") + ".txt";
        } else {
            titel = titel + ".txt";
        }
        DialogZiel dialog = new DialogZiel(paFrame, true, pfad + File.separator + titel, "Infos speichern");
        dialog.setVisible(true);
        if (!dialog.ok) {
            return;
        }
        try {
            Path path = Paths.get(dialog.ziel);
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(Files.newOutputStream(path))));
            if (film != null) {
                br.write(DatenFilm.FILM_SENDER + ":      " + film.arr[DatenFilm.FILM_SENDER_NR]);
                br.write("\n");
                br.write(DatenFilm.FILM_THEMA + ":       " + film.arr[DatenFilm.FILM_THEMA_NR]);
                br.write("\n\n");
                br.write(DatenFilm.FILM_TITEL + ":       " + film.arr[DatenFilm.FILM_TITEL_NR]);
                br.write("\n\n");
                br.write(DatenFilm.FILM_DATUM + ":       " + film.arr[DatenFilm.FILM_DATUM_NR]);
                br.write("\n");
                br.write(DatenFilm.FILM_ZEIT + ":        " + film.arr[DatenFilm.FILM_ZEIT_NR]);
                br.write("\n");
                br.write(DatenFilm.FILM_DAUER + ":       " + film.arr[DatenFilm.FILM_DAUER_NR]);
                br.write("\n");
                br.write(DatenDownload.DOWNLOAD_GROESSE + ":  " + film.arr[DatenFilm.FILM_GROESSE_NR]);
                br.write("\n\n");

                br.write(DatenFilm.FILM_WEBSEITE + "\n");
                br.write(film.arr[DatenFilm.FILM_WEBSEITE_NR]);
                br.write("\n\n");
            }

            br.write(DatenFilm.FILM_URL + "\n");
            br.write(film.arr[DatenFilm.FILM_URL_NR]);
            br.write("\n\n");
            if (!film.arr[DatenFilm.FILM_URL_RTMP_NR].isEmpty()) {
                br.write(DatenFilm.FILM_URL_RTMP + "\n");
                br.write(film.arr[DatenFilm.FILM_URL_RTMP_NR]);
                br.write("\n\n");
            }

            if (film != null) {
                int anz = 0;
                for (String s : film.arr[DatenFilm.FILM_BESCHREIBUNG_NR].split(" ")) {
                    anz += s.length();
                    br.write(s + " ");
                    if (anz > 50) {
                        br.write("\n");
                        anz = 0;
                    }
                }
            }
            br.write("\n\n");
            br.flush();
            br.close();
        } catch (IOException ex) {
            Log.fehlerMeldung(632656214,  "MVInfoFile.writeInfoFile", dialog.ziel);
        }
    }

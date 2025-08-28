public static void writeInfoFile(JFrame paFrame, Daten daten, DatenFilm film) 
{
    try {
        new File(datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD_NR]).mkdirs();
        Path path = Paths.get(datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD_DATEINAME_NR] + ".txt");
        BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(Files.newOutputStream(path))));
        if (datenDownload.film != null) {
            br.write(DatenFilm.FILM_SENDER + ":      " + datenDownload.film.arr[DatenFilm.FILM_SENDER_NR]);
            br.write("\n");
            br.write(DatenFilm.FILM_THEMA + ":       " + datenDownload.film.arr[DatenFilm.FILM_THEMA_NR]);
            br.write("\n\n");
            br.write(DatenFilm.FILM_TITEL + ":       " + datenDownload.film.arr[DatenFilm.FILM_TITEL_NR]);
            br.write("\n\n");
            br.write(DatenFilm.FILM_DATUM + ":       " + datenDownload.film.arr[DatenFilm.FILM_DATUM_NR]);
            br.write("\n");
            br.write(DatenFilm.FILM_ZEIT + ":        " + datenDownload.film.arr[DatenFilm.FILM_ZEIT_NR]);
            br.write("\n");
            br.write(DatenFilm.FILM_DAUER + ":       " + datenDownload.film.arr[DatenFilm.FILM_DAUER_NR]);
            br.write("\n");
            br.write(DatenDownload.DOWNLOAD_GROESSE + ":  " + datenDownload.mVFilmSize);
            br.write("\n\n");
            br.write(DatenFilm.FILM_WEBSEITE + "\n");
            br.write(datenDownload.film.arr[DatenFilm.FILM_WEBSEITE_NR]);
            br.write("\n\n");
        }
        br.write(DatenDownload.DOWNLOAD_URL + "\n");
        br.write(datenDownload.arr[DatenDownload.DOWNLOAD_URL_NR]);
        br.write("\n\n");
        if (!datenDownload.arr[DatenDownload.DOWNLOAD_URL_RTMP_NR].isEmpty() && !datenDownload.arr[DatenDownload.DOWNLOAD_URL_RTMP_NR].equals(datenDownload.arr[DatenDownload.DOWNLOAD_URL_NR])) {
            br.write(DatenDownload.DOWNLOAD_URL_RTMP + "\n");
            br.write(datenDownload.arr[DatenDownload.DOWNLOAD_URL_RTMP_NR]);
            br.write("\n\n");
        }
        if (datenDownload.film != null) {
            int anz = 0;
            for (String s : datenDownload.film.arr[DatenFilm.FILM_BESCHREIBUNG_NR].split(" ")) {
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
        Log.fehlerMeldung(975410369, "StartetClass.writeInfoFile", datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD_DATEINAME_NR]);
    }
}
package cds.util;

public class Utility {

    // Gestion du niveau de trace
    static final int MAXLEVELTRACE = 6;
    static public int levelTrace = 0;


    /**
     * Affichage des message de debugging. Si n est >= au niveau courant
     * le message sera affiche sur la sortie standard
     *
     * @param n Le niveau de debogage
     * @param s Le message a afficher
     */
    static final public void trace(int n, String s) {
        if (n > levelTrace) return;
        s = n == 1 ? ".    " + s + "..."
                : n == 2 ? "--   " + s
                : n == 3 ? "***  " + s
                : ">>>> " + s;
        System.out.println(s);
        //      if( n>2 && aladin!=null && aladin.console!=null ) aladin.console.setInPad(s+"\n");
    }

}

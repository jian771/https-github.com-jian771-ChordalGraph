import java.math.BigInteger;

public class Main {

    static int w;

    static BigInteger[][] tableChoose;
    static BigInteger[] tableChordal;
    static BigInteger[] tableChordalConn;
    static BigInteger[][][][] tableG, tableGTilde, tableGHat;
    static BigInteger[][][] tableG1Tilde, tableG2Tilde;
    static BigInteger[][][][] tableF, tableFTilde, tableFHat;
    static BigInteger[][][][][] tableFHat5;

    static void chooseInit(int n) {
        tableChoose = new BigInteger[n + 1][n + 1];
        tableChoose[0][0] = BigInteger.ONE;
        for (int m = 1; m <= n; m++) {
            tableChoose[m][0] = BigInteger.ONE;
            for (int k = 1; k <= m; k++) {
                BigInteger left = (k <= m - 1) ? tableChoose[m - 1][k] : BigInteger.ZERO;
                BigInteger right = tableChoose[m - 1][k - 1];
                tableChoose[m][k] = left.add(right);
            }
        }
    }

    static BigInteger choose(int n, int k) {
        if (n < 0 || k < 0 || n < k) return BigInteger.ZERO;
        return tableChoose[n][k];
    }

    static BigInteger chordalConn(int k) {
        if (tableChordalConn[k] == null) tableChordalConn[k] = compChordalConn(k);
        return tableChordalConn[k];
    }
    static BigInteger compChordalConn(int k) {
        BigInteger ans = BigInteger.ZERO;
        for (int t = 1; t <= k; t++)
            for (int l = 1; l <= k; l++)
                ans = ans.add(choose(k, l).multiply(f(t, 0, l, k - l)));
        return ans;
    }

    static BigInteger chordal(int k) {
        if (tableChordal[k] == null) tableChordal[k] = compChordal(k);
        return tableChordal[k];
    }
    static BigInteger compChordal(int k) {
        if (k == 0) return BigInteger.ONE;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++)
            ans = ans.add(choose(k - 1, kk - 1).multiply(chordalConn(kk)).multiply(chordal(k - kk)));
        return ans;
    }

    static BigInteger g(int t, int x, int z, int k) {
        if (tableG[t][x][z][k] == null) tableG[t][x][z][k] = compG(t, x, z, k);
        return tableG[t][x][z][k];
    }
    static BigInteger compG(int t, int x, int z, int k) {
        if (t == 0) return k == 0 ? BigInteger.ONE : BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 0; kk <= k; kk++)
            ans = ans.add(choose(k, kk).multiply(gTilde(t, x, z, kk)).multiply(g(t - 1, x, z, k - kk)));
        return ans;
    }

    static BigInteger gTilde(int t, int x, int z, int k) {
        if (tableGTilde[t][x][z][k] == null) tableGTilde[t][x][z][k] = compGTilde(t, x, z, k);
        return tableGTilde[t][x][z][k];
    }
    static BigInteger compGTilde(int t, int x, int z, int k) {
        if (k == 0) return BigInteger.ONE;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++)
            for (int xx = 1; xx <= x; xx++)
                ans = ans.add(choose(k - 1, kk - 1).multiply(choose(x, xx).subtract(choose(z, xx)))
                        .multiply(g1Tilde(t, xx, kk)).multiply(gTilde(t, x, z, k - kk)));
        return ans;
    }

    static BigInteger gHat(int t, int x, int z, int k) {
        if (tableGHat[t][x][z][k] == null) tableGHat[t][x][z][k] = compGHat(t, x, z, k);
        return tableGHat[t][x][z][k];
    }
    static BigInteger compGHat(int t, int x, int z, int k) {
        if (k == 0) return BigInteger.ONE;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++)
            for (int xx = 1; xx < x; xx++)
                ans = ans.add(choose(k - 1, kk - 1).multiply(choose(x, xx).subtract(choose(z, xx)))
                        .multiply(g1Tilde(t, xx, kk)).multiply(gHat(t, x, z, k - kk)));
        return ans;
    }

    static BigInteger g1Tilde(int t, int x, int k) {
        if (tableG1Tilde[t][x][k] == null) tableG1Tilde[t][x][k] = compG1Tilde(t, x, k);
        return tableG1Tilde[t][x][k];
    }
    static BigInteger compG1Tilde(int t, int x, int k) {
        if (k == 0 || t == 0) return BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        for (int l = 1; l <= k; l++)
            ans = ans.add(choose(k, l).multiply(f(t, x, l, k - l)));
        return ans;
    }

    static BigInteger g2Tilde(int t, int x, int k) {
        if (tableG2Tilde[t][x][k] == null) tableG2Tilde[t][x][k] = compG2Tilde(t, x, k);
        return tableG2Tilde[t][x][k];
    }
    static BigInteger compG2Tilde(int t, int x, int k) {
        if (k == 0 || t == 0) return BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk < k; kk++)
            ans = ans.add(choose(k - 1, kk - 1).multiply(g1Tilde(t, x, kk))
                    .multiply(g1Tilde(t, x, k - kk).add(g2Tilde(t, x, k - kk))));
        return ans;
    }

    static BigInteger f(int t, int x, int l, int k) {
        if (tableF[t][x][l][k] == null) tableF[t][x][l][k] = compF(t, x, l, k);
        return tableF[t][x][l][k];
    }
    static BigInteger compF(int t, int x, int l, int k) {
        if (x + l > w) return BigInteger.ZERO;
        if (t == 0) return BigInteger.ZERO;
        if (t == 1) return k == 0 ? BigInteger.ONE : BigInteger.ZERO;
        if (k == 0) return BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++)
            ans = ans.add(choose(k, kk).multiply(fTilde(t, x, l, kk)).multiply(g(t - 2, x + l, x, k - kk)));
        return ans;
    }

    static BigInteger fTilde(int t, int x, int l, int k) {
        if (tableFTilde[t][x][l][k] == null) tableFTilde[t][x][l][k] = compFTilde(t, x, l, k);
        return tableFTilde[t][x][l][k];
    }
    static BigInteger compFTilde(int t, int x, int l, int k) {
        if (t == 0 || t == 1 || k == 0) return BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        ans = ans.add(fHat(t, x, l, k));
        for (int kk = 1; kk < k; kk++)
            ans = ans.add(choose(k, kk).multiply(g1Tilde(t - 1, x + l, kk)).multiply(fHat(t, x, l, k - kk)));
        for (int kk = 1; kk <= k; kk++)
            ans = ans.add(choose(k, kk).multiply(g2Tilde(t - 1, x + l, kk)).multiply(gHat(t - 1, x + l, x, k - kk)));
        return ans;
    }

    static BigInteger fHat(int t, int x, int l, int k) {
        if (tableFHat[t][x][l][k] == null) tableFHat[t][x][l][k] = compFHat(t, x, l, k);
        return tableFHat[t][x][l][k];
    }
    static BigInteger compFHat(int t, int x, int l, int k) { return fHat(t, x, x, l, k); }

    static BigInteger fHat(int t, int x, int z, int l, int k) {
        if (tableFHat5[t][x][z][l][k] == null) tableFHat5[t][x][z][l][k] = compFHat(t, x, z, l, k);
        return tableFHat5[t][x][z][l][k];
    }
    static BigInteger compFHat(int t, int x, int z, int l, int k) {
        if (t == 0 || t == 1 || k == 0) return BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++) {
            for (int xx = 0; xx <= x; xx++) {
                for (int ll = 0; ll <= l; ll++) {
                    if (xx + ll == 0 || xx + ll == x + l) continue;
                    BigInteger prod = choose(k - 1, kk - 1).multiply(choose(l, ll));
                    BigInteger xTerm = (ll > 0) ? choose(x, xx) : choose(x, xx).subtract(choose(z, xx));
                    prod = prod.multiply(xTerm).multiply(g1Tilde(t - 1, xx + ll, kk));
                    BigInteger rest = (ll < l) ? fHat(t, x + ll, z, l - ll, k - kk) : gHat(t - 1, x + ll, z, k - kk);
                    prod = prod.multiply(rest);
                    ans = ans.add(prod);
                }
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        // Hier einfach die gewuenschte Obergrenze aendern:
        int maxN = 35;

        w = maxN; // keine Farbbeschraenkung (volle Zaehlung a(n)/c(n))

        chooseInit(maxN);
        tableChordal = new BigInteger[maxN + 1];
        tableChordalConn = new BigInteger[maxN + 1];
        tableG = new BigInteger[maxN + 1][maxN + 1][maxN + 1][maxN + 1];
        tableGTilde = new BigInteger[maxN + 1][maxN + 1][maxN + 1][maxN + 1];
        tableGHat = new BigInteger[maxN + 1][maxN + 1][maxN + 1][maxN + 1];
        tableG1Tilde = new BigInteger[maxN + 1][maxN + 1][maxN + 1];
        tableG2Tilde = new BigInteger[maxN + 1][maxN + 1][maxN + 1];
        tableF = new BigInteger[maxN + 1][maxN + 1][maxN + 1][maxN + 1];
        tableFTilde = new BigInteger[maxN + 1][maxN + 1][maxN + 1][maxN + 1];
        tableFHat = new BigInteger[maxN + 1][maxN + 1][maxN + 1][maxN + 1];
        tableFHat5 = new BigInteger[maxN + 1][maxN + 1][maxN + 1][maxN + 1][maxN + 1];

        System.out.println("n\tc(n)");
        for (int n = 1; n <= maxN; n++) {
            long start = System.currentTimeMillis();
            BigInteger result = chordalConn(n);
            long end = System.currentTimeMillis();
            System.out.println(n + "\t" + result + "\t(" + (end - start) + " ms)");
        }
    }
}

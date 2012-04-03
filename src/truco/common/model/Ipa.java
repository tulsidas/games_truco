package truco.common.model;

import java.util.List;

public enum Ipa {
    ENVIDO(2), REAL_ENVIDO(3), FALTA_ENVIDO(0);

    private int val;

    private Ipa(int val) {
        this.val = val;
    }

    public static int querido(List<Ipa> ipas, int puntosFalta) {
        int ret = 0;
        for (Ipa i : ipas) {
            if (i == FALTA_ENVIDO) {
                return puntosFalta;
            }
            ret += i.val;
        }

        return ret;
    }

    public static int noQuerido(List<Ipa> ipas) {
        if (ipas.size() == 1) {
            return 1;
        }
        else {
            // todo menos el ultimo

            int ret = 0;
            for (int i = 0; i < ipas.size() - 1; i++) {
                ret += ipas.get(i).val;
            }

            return ret;
        }
    }
}
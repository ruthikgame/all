

public class Exam1 {

    // Машинный эпсилон (половина ULP от 1.0)
    static final double u = Math.ulp(1.0) / 2.0;

    static double gamma(int n) {
        return (double) n * u / (1.0 - n * u);
    }

    // Метод вычисления arctg(x)
    static double[] arctg(double x) {
        final int count = 64;
        double[] terms = new double[count];     // Члены ряда
        double[] absTerms = new double[count];  // Абсолютные значения членов

        double x1 = x;       // x^(2k+1)
        double x2 = x * x; // x^2

        for (int k = 0; k < count; k++) {
            double term = ((k % 2 == 0) ? 1 : -1) * x1 / (2 * k + 1);
            terms[k] = term;
            absTerms[k] = Math.abs(term);
            x1 *= x2;
        }

        double absSum = pairwiseSum(absTerms, count); // сумма модулей членов
        double sum = pairwiseSum(terms, count);       // итоговая сумма ряда

        // Оценка ошибок:
        double cutError = x1 / (2 * count + 1); // ошибка усечения (остаточный член)
        double repError = x * u;                    // ошибка представления первого члена
        // все целые числа <= 2^53 точно представимы
        // поэтому округление знаменателя не требуется
        for (int i = 1; i < count; i++) {
            repError += absTerms[i] * gamma(2 + 4 * i);
        }

        double roundError = absSum * gamma(6); // gamma(log2(64)) = gamma(6)

        double totalError = repError + roundError + cutError;

        return new double[]{sum, totalError};
    }



    // Попарное суммирование для повышения точности
    static double pairwiseSum(double[] array, int length) {
        while (length > 1) {
            int newLength = (length + 1) / 2;
            for (int i = 0; i < length / 2; i++) {
                array[i] = array[2 * i] + array[2 * i + 1];
            }
            if (length % 2 == 1) {
                array[newLength - 1] = array[length - 1];
            }
            length = newLength;
        }
        return array[0];
    }

    public static void main(String[] args) {
        double x1 = 1.0 / 5.0;
        double x2 = 1.0 / 239.0;

        double[] a1 = arctg(x1);
        double[] a2 = arctg(x2);

        // Формула Мачина: π = 16 * arctg(1/5) - 4 * arctg(1/239)
        double piApprox = 16 * a1[0] - 4 * a2[0];

        // Оценка абсолютной ошибки: масштабируем индивидуальные ошибки и добавляем ULP от суммы
        double absError = (16 * a1[1]) + (4 * a2[1]) + piApprox * u;

        System.out.printf("Приближённое значение π: %.20f%n", piApprox);
        System.out.printf("Оценка абсолютной погрешности: %.20e%n", absError);
    }
}

package ппаы;

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
        // это тоже самое, что ошибка представления x
        // поэтому ее можно отдельно не учитывать
        double repError = x * u;                    // ошибка представления первого члена
        // почему такой аргумент у гаммы
        // 1 | x
        // 6 | x * x * x / 3
        // 10 | x * x * x * x * x / 5
        // 14 | x * x * x * x * x * x * x / 7
        // все целые числа <= 2^53 точно представимы
        // поэтому округление знаменателя не требуется
        for (int i = 1; i < count; i++) {//мы оцениваем ошибку сверху, поэтому неважно, округляется в большую или меньшую сторону оцениваем с запасом
            repError += absTerms[i] * gamma(2 + 4 * i);//это консервативная оценка, учитывающая: 2 операции для начальных вычислений, 4 операции на каждый следующий член
        }//Такой подход даёт верхнюю границу погрешности .Реальная ошибка может быть меньше, но не больше этой оценки.В научных вычислениях принято оценивать именно верхнюю границу.


        // попарное суммирование, поэтому gamma(log2(count))
        // count = 64
        // log_2(64) = 6
        double sumError = absSum * gamma(6); // gamma(log2(64)) = gamma(6) Каждый уровень вносит ошибку порядка gamma(1) (относительная ошибка одного сложения).

        double totalError = repError + sumError + cutError;

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
        // 16 и 4 - точно представимы т.к являются степенями 2, на u умножать не надо
        // Числа с плавающей запятой хранятся в двоичном виде
        // Число вида ±M × 2ᴱ, где M — мантисса, E — экспонента.
        // Умножение на 2ⁿ просто сдвигает экспоненту на n, не затрагивая мантиссу.
        // ошибки умножения: 16 * e1, 4 * e2
        // ошибка вычитания: pi * u
        // Формула Мачина: π = 16 * arctg(1/5) - 4 * arctg(1/239)
        double piApprox = 16 * a1[0] - 4 * a2[0];

        // Оценка абсолютной ошибки: масштабируем индивидуальные ошибки и добавляем ULP от суммы
        double absError = (16 * a1[1]) + (4 * a2[1]) + piApprox * u;

        System.out.printf("Приближённое значение π: %.20f%n", piApprox);
        System.out.printf("Оценка абсолютной погрешности: %.20e%n", absError);
    }
}

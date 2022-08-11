import java.util.concurrent.atomic.AtomicInteger;

public class Concurrency {

    private final static int SIZE = 10_500_000;
    private final static int HALF = SIZE/2;
    public static void main(String[] args) {
        timer();
        withoutConcurrency();
        withConcurrency();
    }

    public static void timer(){
        Thread timer = new Thread(new Runnable(){
            @Override
            public void run() {
                int seconds = 0;
                try {
                    while(true){
                        System.out.println(seconds++);
                        Thread.sleep(1000);
                    }
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        // показываем, что это поток-демон, который должен завершиться после завершения всех остальных (основных) потоков
        timer.setDaemon(true);
        timer.start();
    }

    public static void withoutConcurrency(){
        float[] list1 = new float[SIZE];
        for (int i=0; i<SIZE; i++){
            list1[i] = 1f;
        }
        long before = System.currentTimeMillis();
        for (int i=0; i<list1.length; i++){
            float f = (float) i; // Это обязательно, т.к. int/int = int - округление
            list1[i] = (float) (list1[i]*Math.sin(0.2f+f/5)*Math.cos(0.2f+f/5)*Math.cos(0.4f+f/2));
        }
        long after = System.currentTimeMillis();
        System.out.println("Without Multithreading - " + (after-before));
    }

    public static void withConcurrency() {
        float[] list2 = new float[SIZE];
        for (int i=0; i<SIZE; i++){
            list2[i] = 1f;
        }
        long before = System.currentTimeMillis();
        float[] leftList = new float[HALF];
        System.arraycopy(list2, 0, leftList, 0, HALF);
        float[] rightList = new float[HALF];
        System.arraycopy(list2, HALF, rightList, 0, HALF);
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<HALF; i++){
                    float f = (float) i;
                    leftList[i] = (float) (leftList[i]*Math.sin(0.2f+f/5)*Math.cos(0.2f+f/5)*Math.cos(0.4f+f/2));
                }
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<HALF; i++){
                    float f = (float) i + HALF; // т.к. тут значения должны быть второй половины
                    rightList[i] = (float) (rightList[i]*Math.sin(0.2f+f/5)*Math.cos(0.2f+f/5)*Math.cos(0.4f+f/2));
                }
            }
        });
        thread1.start();
        thread2.start();
        try{
            thread1.join();
            thread2.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.arraycopy(leftList, 0, list2, 0, HALF);
        System.arraycopy(rightList, 0, list2, HALF, HALF);
        long after = System.currentTimeMillis();
        System.out.println("With Multithreading - " + (after-before));
    }
}

class SomeClass{
    // volatile - чтобы какой-то поток не хэшировал эту переменную, а менял её
    // Атомарные типы содержат методы, позволяющие выполнять арифметические операции в 1 операцию, а не в 3, как обычно:
    // получение значения, его изменение, запись обратно.
    private volatile static AtomicInteger value = new AtomicInteger(0);

    public static void main(String[] args) {
        Thread inc = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i<1000000; i++){
                    inc();
                }
            }
        });
        Thread dec = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i<1000000; i++){
                    dec();
                }
            }
        });
        inc.start();
        dec.start();
        try {
            inc.join();
            dec.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        /** Из-за Race Condition здесь могут выходить разные значения */
        System.out.println(value);
    }

    public static void inc(){
        value.getAndIncrement();
    }

    public static void dec(){
        value.getAndDecrement();
    }
}
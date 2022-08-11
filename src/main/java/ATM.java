public class ATM {
    private int money = 3_000_000;
    private final Object monitor = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread user1 = new Thread(new Runner("Jhon", 1_500_000));
        Thread user2 = new Thread(new Runner("Maks", 1_400_000));
        Thread user3 = new Thread(new Runner("Melisa", 500_000));
        long before = System.currentTimeMillis();
        // Здесь может возникнуть проблема состояния гонки - баланс уйдет в минус
        user1.start();
        user2.start();
        user3.start();

        user1.join();
        user2.join();
        user3.join();

        long after = System.currentTimeMillis();
        System.out.println(after-before);

        // Решение проблемы - join | synchronised
        checkRunningTime(ChoseRunner.ONE);
        checkRunningTime(ChoseRunner.TWO);
        checkRunningTime(ChoseRunner.THREE);
    }

    private static void checkRunningTime(ChoseRunner runner) { // Здесь ChoseRunner.ONE TWO ИЛИ THREE
        Thread user1 = runner.getRunner("Jhon", 1_500_000);
        Thread user2 = runner.getRunner("Maks", 1_400_000);
        Thread user3 = runner.getRunner("Melisa", 400_000);
        long before1 = System.currentTimeMillis();
        if(runner == ChoseRunner.ONE){
            // Вот так жутко придется делать без синхронизации
            user1.start();
            try {
                user1.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            user2.start();
            try {
                user2.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            user3.start();
            try {
                user3.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }else{
            user1.start();
            user2.start();
            user3.start();
            try{
                user1.join();
                user2.join();
                user3.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        long after1 = System.currentTimeMillis();
        System.out.println(runner.printText()+(after1-before1));
    }

    enum ChoseRunner{
        ONE, TWO, THREE;

        public Thread getRunner(String name, int request){
            if (this == ONE){
                return new Thread(new Runner(name, request));
            }
            else if (this == TWO){
                return new Thread(new Runner2(name, request));
            }
            else{
                return new Thread(new Runner3(name, request));
            }
        }

        public String printText(){
            if (this == ONE){
                return "Without synchronised ";
            }
            else if (this == TWO){
                return "Synchronised by this ";
            }
            else{
                return "Synchronised by object-monitor ";
            }
        }
    }

    public void getMoney(String name, int request){
        System.out.println(name + " went to ATM");
        try {
            Thread.sleep(2000); // Будет останавливать тот поток, в котором вызван данный метод
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (request <= money){
            money = money-request;
            System.out.printf("%s take %s rubs. In ATM money become - %s rubs.\n", name, request, money);
        }else{
            System.out.println("ATM haven't such money for "+name);
        }
    }

    public synchronized void getMoneySyncUsingThis(String name, int request){
        getMoney(name, request);
    }

    public void getMoneySyncUsingObj(String name, int request){
        synchronized(monitor){
            getMoney(name, request);
        }
    }
}

class Runner implements Runnable{
    static final ATM atm = new ATM();
    String name;
    int request;

    public Runner(String name, int request){
        this.name = name;
        this.request = request;
    }

    @Override
    public void run() {
        atm.getMoney(name, request);
    }
}

class Runner2 implements Runnable{
    static final ATM atm = new ATM();
    String name;
    int request;

    public Runner2(String name, int request){
        this.name = name;
        this.request = request;
    }

    @Override
    public void run() {
        atm.getMoneySyncUsingThis(name, request);
    }
}

class Runner3 implements Runnable{
    static final ATM atm = new ATM();
    String name;
    int request;

    public Runner3(String name, int request){
        this.name = name;
        this.request = request;
    }

    @Override
    public void run() {
        atm.getMoneySyncUsingObj(name, request);
    }
}
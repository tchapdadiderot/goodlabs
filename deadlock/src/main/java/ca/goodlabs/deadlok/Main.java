package ca.goodlabs.deadlok;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {
        final Friend alphonse = new Friend("Alphonse");
        final Friend gaston = new Friend("Gaston");
        new Thread(() -> alphonse.bow(gaston)).start();
        new Thread(() -> gaston.bow(alphonse)).start();
    }

    static class Friend {
        private final String name;
        private final Lock lock = new ReentrantLock();

        public Friend(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void bow(Friend bower) {
            lockSuccessfullyOrWaitForResourceReleasing();
            System.out.format(
                    "%s: %s has bowed to me!%n",
                    this.name,
                    bower.getName()
            );
            bower.bowBack(this);
        }

        private void lockSuccessfullyOrWaitForResourceReleasing() {
            if (!lock.tryLock()) {
                sleep();
            }
        }

        private void sleep() {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void bowBack(Friend bower) {
            lockSuccessfullyOrWaitForResourceReleasing();
            System.out.format(
                    "%s: %s has bowed back to me!%n",
                    this.name,
                    bower.getName()
            );
        }
    }

}
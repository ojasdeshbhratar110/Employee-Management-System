package employee_management;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration FAILURE_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_TRACKED_USERNAMES = 10_000;

    private final Map<String, Deque<Instant>> failures = new LinkedHashMap<>(16, 0.75f, true);

    synchronized long retryAfterSeconds(String username) {
        String key = normalize(username);
        Deque<Instant> attempts = failures.get(key);
        if (attempts == null) {
            return 0;
        }

        Instant now = Instant.now();
        prune(attempts, now);
        if (attempts.isEmpty()) {
            failures.remove(key);
            return 0;
        }
        if (attempts.size() < MAX_FAILED_ATTEMPTS) {
            return 0;
        }

        long seconds = Duration.between(now, attempts.peekFirst().plus(FAILURE_WINDOW)).getSeconds();
        return Math.max(1, seconds + 1);
    }

    synchronized void recordFailure(String username) {
        Instant now = Instant.now();
        String key = normalize(username);
        Deque<Instant> attempts = failures.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        prune(attempts, now);
        attempts.addLast(now);

        while (failures.size() > MAX_TRACKED_USERNAMES) {
            Iterator<String> iterator = failures.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
    }

    synchronized void recordSuccess(String username) {
        failures.remove(normalize(username));
    }

    synchronized void clear() {
        failures.clear();
    }

    private void prune(Deque<Instant> attempts, Instant now) {
        Instant cutoff = now.minus(FAILURE_WINDOW);
        while (!attempts.isEmpty() && !attempts.peekFirst().isAfter(cutoff)) {
            attempts.removeFirst();
        }
    }

    private String normalize(String username) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return normalized.length() > 100 ? normalized.substring(0, 100) : normalized;
    }
}
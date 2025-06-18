package backend.academy.scrapper.service.impl.scheduler;

import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.service.impl.LinkUpdateProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("multi")
public class SchedulerServiceMultiImpl extends AbstractSchedulerService {
    private static final int THREAD_POOL_SIZE = 4;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public SchedulerServiceMultiImpl(ParseRequestRepository repo, LinkUpdateProcessor processor) {
        super(repo, processor);
    }

    @Override
    @Scheduled(fixedRate = 10000)
    public void sendNewAnswers() {
        Map<Link, List<ParseRequest>> linkChats = getLinkChatMap();
        List<Map.Entry<Link, List<ParseRequest>>> batch = new ArrayList<>(linkChats.entrySet());
        List<List<Map.Entry<Link, List<ParseRequest>>>> chunks = splitIntoChunks(batch, THREAD_POOL_SIZE);

        CountDownLatch latch = new CountDownLatch(chunks.size());

        List<Future<?>> futures = new ArrayList<>();

        for (List<Map.Entry<Link, List<ParseRequest>>> chunk : chunks) {
            Future<?> future = executor.submit(() -> {
                try {
                    for (Map.Entry<Link, List<ParseRequest>> entry : chunk) {
                        linkUpdateProcessor.process(entry.getKey(), entry.getValue());
                    }
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        try {
            boolean completed = latch.await(30, TimeUnit.SECONDS); // например, 30 секунд на все задачи
            if (!completed) {
                System.err.println("Timeout occurred while waiting for tasks to finish.");
                for (Future<?> future : futures) {
                    if (!future.isDone()) {
                        future.cancel(true);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread was interrupted while waiting for tasks to finish.");
        }
    }

    private <T> List<List<T>> splitIntoChunks(List<T> list, int parts) {
        List<List<T>> chunks = new ArrayList<>();
        int chunkSize = (int) Math.ceil((double) list.size() / parts);
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return chunks;
    }
}

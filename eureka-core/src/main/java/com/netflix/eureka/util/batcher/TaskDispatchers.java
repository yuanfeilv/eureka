package com.netflix.eureka.util.batcher;

/**
 * See {@link TaskDispatcher} for an overview.
 *
 * @author Tomasz Bak
 * 创建批量任务执行得到分发器
 */
public class TaskDispatchers {

    /**
     *
     * @param id 任务执行编号
     * @param maxBufferSize 代执行队列的最大数量
     * @param workerCount 任务执行器工作线程数
     * @param maxBatchingDelay 批量任务等待最大延迟时间
     * @param congestionRetryDelayMs
     * @param networkFailureRetryMs
     * @param taskProcessor 任务处理器
     * @param <ID> 任务编号泛型
     * @param <T> 任务泛型
     * @return
     */
    public static <ID, T> TaskDispatcher<ID, T> createNonBatchingTaskDispatcher(String id,
                                                                                int maxBufferSize,
                                                                                int workerCount,
                                                                                long maxBatchingDelay,
                                                                                long congestionRetryDelayMs,
                                                                                long networkFailureRetryMs,
                                                                                TaskProcessor<T> taskProcessor) {
        // 创建任务接收器
        final AcceptorExecutor<ID, T> acceptorExecutor = new AcceptorExecutor<>(
                id, maxBufferSize, 1, maxBatchingDelay, congestionRetryDelayMs, networkFailureRetryMs
        );
        // 创建任务分发器
        final TaskExecutors<ID, T> taskExecutor = TaskExecutors.singleItemExecutors(id, workerCount, taskProcessor, acceptorExecutor);
        // 创建任务执行器
        return new TaskDispatcher<ID, T>() {
            @Override
            public void process(ID id, T task, long expiryTime) {
                acceptorExecutor.process(id, task, expiryTime);
            }

            @Override
            public void shutdown() {
                acceptorExecutor.shutdown();
                taskExecutor.shutdown();
            }
        };
    }

    public static <ID, T> TaskDispatcher<ID, T> createBatchingTaskDispatcher(String id,
                                                                             int maxBufferSize,
                                                                             int workloadSize,
                                                                             int workerCount,
                                                                             long maxBatchingDelay,
                                                                             long congestionRetryDelayMs,
                                                                             long networkFailureRetryMs,
                                                                             TaskProcessor<T> taskProcessor) {
        final AcceptorExecutor<ID, T> acceptorExecutor = new AcceptorExecutor<>(
                id, maxBufferSize, workloadSize, maxBatchingDelay, congestionRetryDelayMs, networkFailureRetryMs
        );
        final TaskExecutors<ID, T> taskExecutor = TaskExecutors.batchExecutors(id, workerCount, taskProcessor, acceptorExecutor);
        return new TaskDispatcher<ID, T>() {
            @Override
            public void process(ID id, T task, long expiryTime) {
                acceptorExecutor.process(id, task, expiryTime);
            }

            @Override
            public void shutdown() {
                acceptorExecutor.shutdown();
                taskExecutor.shutdown();
            }
        };
    }
}

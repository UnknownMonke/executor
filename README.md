# Custom Thread Pool Executor

## Features

- Fixed thread pool.
- Task queue.
- Basic `execute`.
- Worker threads.
- Graceful shutdown.
- Support `submit` returning `Future`.
- **Rejection policies**.
- Monitoring (active threads, completed tasks, queue size...).
- **Priority management**.
- Dynamic aging (waiting time boosts priority).
- Scheduled tasks (deadlines).

#
### Ideas for future features

- Exception handling per worker.
- Idle thread timeout.
- Dynamic thread pool.
- Custom thread factory.
- Security context propagation.
- Deadline-based cancellation.
- SLA violation handler.
- Task timeout enforcement.
- Adaptive priority boost based on urgency.
- Early SLA breach warnings.
- Metric collector.
- Queue visualizer.

#
### To do

- **Locking implementation**.
- **Tests**.
- **Performance** scenarios with JMH.
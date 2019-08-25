# Task Scheduler - Group 13
This project is about using artificial intelligence (AI) and parallel processing power to solve a
difficult scheduling problem.

[![CircleCI](https://circleci.com/gh/jordansimsmith/task-scheduler/tree/master.svg?style=svg&circle-token=c48171477558fe26614b73a159c316c8658e152b)](https://circleci.com/gh/jordansimsmith/task-scheduler/tree/master)

![task_scheduler](https://user-images.githubusercontent.com/18223858/63645326-a53b9500-c74f-11e9-9061-ce0cea30c0a9.png)

## How to build the project
The project was developed in Java, using Maven as a build tool. Run `mvn package` to ensure dependencies, run unit tests and package the runnable jar file. The runnable jar will be located at `target/task-scheduler-1.0-SNAPSHOT.jar`. 

## How to run the project
```
java −jar scheduler.jar INPUT.dot P [OPTION]
INPUT.dot a task graph with integer weights in dot format
P number of processors to schedule the INPUT graph on

Optional: 
−p N use N cores for execution in parallel (default is sequential )
−v visualise the search
−o OUTPUT.dot output file is named OUTPUT.dot (default is INPUT−output.dot)
```
To run the project, ensure you have Oracle JDK 8 installed. Refer to the help message above for command line arguments. The project is developed to run on Linux, specifically the UoA lab computer builds. For visualisation, make sure you have mpstat (from the sysstat package) installed if running in another Linux environment. 

## Project documentation
Please refer to the project Wiki for comprehensive documentation regarding the development of this project. Key areas of the wiki are linked below.
- [Waterfall Project Plan](https://github.com/jordansimsmith/task-scheduler/wiki/Project-Plan)
- [Software Architecture UML Diagram](https://github.com/jordansimsmith/task-scheduler/wiki/High-Level-Design-(HLD)-UML-Diagram)
- [Weekly Progress Updates](https://github.com/jordansimsmith/task-scheduler/wiki/Weekly-Progress-Updates)
- [Meeting Minutes](https://github.com/jordansimsmith/task-scheduler/wiki/Meeting-Agendas-and-Minutes)
- [User Interface Requirements](https://github.com/jordansimsmith/task-scheduler/wiki/Visualization-Requirements)
- [Visualisation Mock-ups](https://github.com/jordansimsmith/task-scheduler/wiki/Visualization-Mock-ups)

Project boards, issue tracking and pull requests were used to aid project management. Please refer to the links below.
- [Project Board](https://github.com/jordansimsmith/task-scheduler/projects/1)
- [Issues](https://github.com/jordansimsmith/task-scheduler/issues?utf8=%E2%9C%93&q=)
- [Pull Requests](https://github.com/jordansimsmith/task-scheduler/pulls?utf8=%E2%9C%93&q=)

## Notes regarding the user interface
![task_scheduler_ui](https://user-images.githubusercontent.com/18223858/63645426-40ce0500-c752-11e9-8037-2fe99f3f9286.png)
### Features
- **Input Graph:** This panel is a visual representation of the input graph provided by the user.
- **Current Schedule:** This panel represents the schedule that is currently being considered by the scheduling algorithm. It will continue to change until the optimal schedule is found. Each task can be clicked on to show its parents and children's locations on the current schedule. Please refer to the above image for an example.
- **States Searched:** This panel provides information on the algorithm's progress. This includes the algorithms state (running/finished), the time elapsed, and the proportion of the total search space processed. This proportion is shown by the pie graph on the left hand side, and is log transformed to provide a clearer visual indication.
- **CPU/Memory:** These panels show the current performance of the hardware while Task Scheduler is active.

### Performance Disclaimer
When the user interface is enabled (`-v` flag), Task Scheduler throttles/uses a less intensive algorithm to provide a smoother user experience. 
> **If timing is critical, please run the program without visualisation enabled.**

## Team members
- Reshad Contractor - res550 - Rcon954
- Harrison Leach - HarrisonLeach1 - Hlea849
- Nidhinesh Nand - nidhineshnand - Nnan773
- Jed Robertson - JedLJRobertson - Jrob928
- Jordan Sim-Smith - jordansimsmith - Jsim862

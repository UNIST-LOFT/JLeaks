# JLeaks: A Featured Resource Leak Repository Collected From Hundreds of Open-Source Java Projects
This repository is modified version of original JLeaks repository, which can be found at: https://github.com/Dcollectors/JLeaks.

We added a JSON file to parse the data more easily.
It is located in `src/jleaks/resources/jleaks.json`.

We also added a Defects4j-style framework to use JLeaks more easily.

## To install
We does not support installing package yet.
You can clone the repository and add `PYTHONPATH` environment variable to use it.

```bash
git clone https://github.com/UNIST-LOFT/JLeaks.git
cd JLeaks
export PYTHONPATH=$PYTHONPATH:$PWD/src
```

## How to use
Basically, you can run `jleaks` module with python.
```bash
python3 -m jleaks <sub-command> [options...]
```

Currently, we support the sub-commands below:
- `info`: Show the summary of JLeaks dataset or a single leak.

### `info`
Show the summary of JLeaks dataset or a single leak.
```bash
python3 -m jleaks info [-l|--leak-id LEAK_ID]
```

If you do not provide `--leak-id` option, it shows the number of leaks per project.
On the other hand, if you provide `--leak-id` option, it shows the detailed information of the leak.

# README from original JLeaks

# JLeaks: A Featured Resource Leak Repository Collected From Hundreds of Open-Source Java Projects
JLeaks is a resource leaks repository collected from real-world projects which facilitates in-depth researches and evaluation of defect-related algorithms. Each defect in Leaks includes four aspects key information: project information, defect information, code characteristics, and file information. We will continuously update the repository as much as possible.

- [Repository Structure](#repository-structure)
- [Contents of JLeaks](#contents-of-jleaks)
- [References](#references)



## Repository Structure
```
├─ JLeaksDataset                   // full data
│  ├─ bug_methods.zip           // faulty methods
│  └─ fix_methods.zip           // fixed methods
│  └─ bug_files.zip            // faulty files
│  └─ files.zip            // fixed files
│  └─ bug_bytecode_files.zip       // faulty bytecode files               
```

## Contents of JLeaks
So far, JLeaks contains **`1,160`** real-world resource leaks from 321 open-source Java projects. Detailed information about these defects can be found in the **`JLeaks.xlsx`**.

Item  |  Description
----------------------- | -----------------------
ID                      | defect ID
projects                | Github project name in the format owner/repository (e.g., "aaberg/sql2o")
commit url              | the URL including the commit details, defect code, and patch code
UTC of buggy commit     | UTC of defect code submission
UTC of fix commit       | UTC of fixed code submission
start line              | the start line of defect method
end line                | the end line of defect method
defect method           | the location and name of defect method (e.g., "src/main/java/org/sql2o/Query.java:executeAndFetchFirst")
change lines            | the change line between defect code and fixed code (e.g., "src/main/java/org/sql2o/Query.java:@@ -271,151 +271,180 @@")
resource types          | the type of system resource (options: **`file`**, **`socket`**, and **`thread`**)
root causes             | root causes of defect.
fix approaches          | approaches used to fixed the defect
patch error?        | indication of whether the patch is correct or not
standard libraries      | standard libraries related to defects
third-party libraries   | third-party libraries related to defects
is inter-procedural     | whether the resource leak is inter-procedural
key variable name       | the name of the key variable holding the system resource
key variable location   | the location of key variable (e.g., "src/main/java/org/sql2o/Query.java:413")
key variable attribute  | the attribute of key variable (options: **`anonymous variable`**, **`local variable`**, **`parameter`**, **`class variable`**, and **`instance variable`**) 

The root causes are displayed in the table below.
Causes  |  Description
------------- | -------------
noCloseEPath  | No close on exception paths
noCloseRPath  | No close on regular paths
notProClose   | Not provided close()
noCloseCPath  | No close for all branches paths

The fixed approaches are shown in the table below.
Fixed Approaches  |  Description
--------------- | ---------------
try-with        | Use try-with-resources
CloseInFinally  | Close in finally
CloseOnEPath    | Close on exception paths
CloseOnRPath    | Close on regular paths
AoRClose        | Add or rewrite close

## References
[1] Roland Croft, Muhammad Ali Babar, and M. Mehdi Kholoosi. 2023. Data Quality for Software Vulnerability Datasets. In 45th IEEE/ACM International Conference on Software Engineering, ICSE 2023, Melbourne, Australia, May 14-20, 2023. IEEE, 21–133. https://doi.org/10.1109/ICSE48619.2023.00022

[2] 2008. ISO/IEC 25012:2008 - Systems and software engineering – Software product Quality Requirements and Evaluation (SQuaRE) – Data quality model. International Organization for Standardization. https://www.iso.org/standard/35736.html

[3] Miltiadis Allamanis. 2019. The adverse effects of code duplication in machine learning models of code. In Proceedings of the 2019 ACM SIGPLAN International Symposium on New Ideas, New Paradigms, and Reflections on Programming and Software, Onward! 2019, Athens, Greece, October 23-24, 2019, Hidehiko Masuhara and Tomas Petricek (Eds.). ACM, 143–153. https://doi.org/10.1145/3359591.3359735

[4] Yepang Liu, Jue Wang, Lili Wei, Chang Xu, Shing-Chi Cheung, Tianyong Wu, Jun Yan, and Jian Zhang. 2019. DroidLeaks: a comprehensive database of resource leaks in Android apps. Empir. Softw. Eng. 24, 6 (2019), 3435–3483. https://doi.org/10.1007/s10664-019-09715-8

[5] pmd. 2023. GitHub - pmd/pmd: An extensible multilanguage static code analyzer.https://github.com/pmd/pmd. (Accessed on 03/30/2023).

[6] FaceBook. 2023. GitHub - facebook/infer: A static analyzer for Java, C, C++, and Objective-C. https://github.com/facebook/infer. (Accessed on 03/30/2023).

[7] SpotBugs. 2023. SpotBugs. https://spotbugs.github.io/. (Accessed on 03/30/2023).

[8] Jie Wang, Wensheng Dou, Yu Gao, Chushu Gao, Feng Qin, Kang Yin, Jun Wei: A comprehensive study on real world concurrency bugs in Node.js. ASE 2017: 520-531

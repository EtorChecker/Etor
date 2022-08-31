# Etor
Etor is a detecting tool for the unethical behavior issues in GitHub repositories.

# Usage
Etor is in Java, and Python. You can find the source code under "src" directory and the required scripts for running the tool under the "tool" directory.

1. After cloning/extracting the tool directory in a folder (e.g., ~/Etor/tool), you need to change the ***PROJECTLOCATION***, and ***PYTHONLOCATION*** parameters in **etor.sh**, and **GetFileUpdatedDate.py** script files. ***PROJECTLOCATION*** is the path of your folder where the tool (Etor) located, and ***PYTHONLOCATION*** is the path of your PYTHON folder.

2. Run the script for the app using the following command. Etor can detect 6 unethical behavior issues types.
>./Etor.sh <github_token_to_authenticate> <type_of_unethical_issue> <repoistory_1> <repository_2> <GitHub_Issue_PR_link>

  For "Unmaintained-Android-Project-with-PaidService",
  >[E.g.,  ./Etor.sh ghp_xxx 1 https://github.com/EtorChecker/Etor null null]
  
  
  For "No-License-Provided-for-Public-Repository",
  >[E.g.,  ./Etor.sh ghp_xxx 2 https://github.com/EtorChecker/Etor null null]
  
  For "Uninformed-License-Change",
  >[E.g.,  ./Etor.sh ghp_xxxx 4 https://github.com/EtorChecker/Etor null null]
  
  For "No-Attribution-To-the-Author-in-Code",
  >[E.g.,  ./Etor.sh ghp_xxx 5 https://github.com/EtorChecker/Etor null https://github.com/EtorChecker/Etor/issues/2]
  
   For "Soft-Forking",
  >[E.g.,  ./Etor.sh ghp_xxx 6 https://github.com/EtorChecker/Etor https://github.com/EtorChecker/EtorCopy null]
  
   For "Self-Promotion",
  >[E.g.,  ./Etor.sh ghp_xxx 7 null null https://github.com/EtorChecker/Etor/issues/2]  

# Replication package
The replication package is structured as follows:

```
    /
    .
    |--- data/                   List of study, and evaluation data. 
    |--- lib/                    List of dependencies.   
    |--- src/                    Implementation including libraries and source code
    |--- tool/                   The scripts
    |--- LicenseList.txt         The list of license
```

[This is an external link to download Etor.jar](https://www.dropbox.com/s/ul5b6k8sdqxlpcd/Etor.jar?dl=0)

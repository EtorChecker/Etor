#import sys
#sys.path.append('C:\Users\Heather\AppData\Local\Programs\Python\Python38\GitPython')
import time
from github import Github
g = Github("token")
f = open("C:\\Users\\Etor\\eclipse-workspace\\Etor\\Repo.txt", "r")
ii=0
for x in f:
 #print(x)
 ii=ii+1
 if ii%30==0:
  time.sleep(60)
 p = x.split() 
 repo = g.get_repo(str(p[0]))
 #print(repo.name)
 commits = repo.get_commits(path=p[1])
 #print(commits.totalCount)
 f = open('C:\\Users\\Etor\\eclipse-workspace\\Etor\\CommitListTest.txt','a', encoding="utf-8")
 if commits.totalCount>1:
  i=commits.totalCount
  for commit in commits:
   if i>1:
    f.write(str(p[0])+"|"+str(p[1])+"|"+str(commit.commit.committer.date)+"|"+str(commit.commit.author.date)+"|"+str(commit.commit.author)+"|"+str(commit.commit.sha)+"\n")
   i=i-1   

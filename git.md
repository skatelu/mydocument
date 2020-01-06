# git

## 三棵树











## git 上传代码相关流程与代码

1.  首先在git目录中运行 git status  查看文件不同

2. git diff //可以查看工作树，暂存区，最新提交之间的差别

3. 添加到暂存区  

   * 用git add -u 把所有修改的文件提交到版本库放入暂存

   * git add. 他会监控工作区的状态树，使用它会把工作时的**所有变化提交**到暂存区，包括文件内容修改(modified)以及新文件(new)，但不包括被删除的文件。

4. git status  查看状态

5. git commit -m "update"



* 删除git 本地分支：
  * git branch -d [分支名称]  
  * git branch -D [分支名称]          强制删除

## 将远程代码检出到本地分支：	

* git tag  查看代码的 tag
* git checkout -b [本地分支名称]  [tag名]
* git checkout -b [本地分支名称] origin/[远程分支name]

**给本地分支换名字**

* 如果对于分支不是当前分支，可以使用下面代码：
  * git branch -m [原分支名] [新分支名]

* 如果是当前分支，那么可以使用加上新名字：
  * git branch -m [新支名称]

**将本地分支提交到远程**

* 提交本地test分支作为远程的test分支：
  * $ **git push** origin test:test	
* 提交本地test分支作为远程的master分支 //好像只写这一句，远程的github就会自动创建一个test分支
  * $ **git push** origin test:master
* 刚提交到远程的test将被删除，但是本地还会保存的，不用担心
  * $ **git push** origin :test 

**修改制定的远程分支的代码**

* 首先用checkout命令将本地git切换到需要修改的远程分支下面

* 然后直接对代码进行修改，修改完成后，将代码进行提交

  * ```json
    git status
    git diff
    git add .
    git commit -m ""
    git status
    git push
    git poll
    ```

  

* You have not concluded your merge. (MERGE_HEAD exists) 问题

  * 本地有修改和提交，如何强制用远程的库更新更新。

*  正确的做法应该是：

  ```json
  git fetch --all
  git reset --hard origin/master
  ```

  git fetch 只是下载远程的库的内容，不做任何的合并git reset 把HEAD指向刚刚下载的最新的版本

* 创建本地分支
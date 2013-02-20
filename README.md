<h1>Section 10 Project 4</h1>

<h2>Overview</h2>

In the projects we have done upto now, we have created three mock classes for testing:

 - ```HardcodedMineInitializationStrategy.java```
 - ```MockPersistenceStrategy.java```
 - ```MockOptionPane.java```

Even is a small project such as this, we had to create three mock classes for helping in the tests. Imagine a larger project where we might have to create fifty or even five hundred mock classes. This would be a huge impediment to developers writing unit tests. In all likelihood, unit tests might be entirely sacrificed due to the effort required to write and maintain them.

It would be wonderful if we had a mechanism to generate mocks on the fly, without really having to manually create those classes. Well, here's some good news for us developers. There actually is a library which does exactly that. In fact there are two such libraries – [Mockito](http://code.google.com/p/mockito/) and [EasyMock](http://www.easymock.org/). In this project we will use EasyMock to create mock classes on the fly, instead of creating separate ones for each mock.

In this project, we have deleted all the three mock classes, and given you a project with compile errors as well as failing unit tests. However, we will not leave you totally high and dry in this activity. We have refactored all unit tests in the com.diycomputerscience.minesweeper package to use mock classes. You have to take the cue from them and implement mock classes in all test cases in the  com.diycomputerscience.minesweeper.view package.

In this project, you will have to do some amount of reading code and understanding what is happening. Also read up on the [documentation of EasyMock 3.1](http://www.easymock.org/EasyMock3_1_Documentation.html) to get a baseline understanding of how to use it. 

One final note - don't let the exercise of reading code frustrate you. Feel free to ask questions on the forum, and trust us, reading and understanding code is vital to become a good software developer.

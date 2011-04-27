package com.algoTrader.util;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

public class AntLauncher {

    public static void launch(Class<?> clazz, String[] args, String[] vmArgs) {

        Project project = new Project();
        project.setBaseDir(new File(System.getProperty("user.dir")));
        project.init();

        try {

            Java java = new Java();
            java.setProject(project);
            java.setFork(true);
            java.setSpawn(true);
            java.setClassname(clazz.getName());

            for (String arg : args) {
                java.createArg().setValue(arg);
            }

            for (String vmArg : vmArgs) {
                java.createJvmarg().setValue("-D" + vmArg);
            }

            // add the classes of this Project
            Path path = new Path(project, new File("classes").getAbsolutePath());

            // get the path to the AlgoTrader project
            String algoTraderPath = System.getenv("ALGOTRADER_HOME");

            // add the classes of AlgoTrader
            path.add(new Path(project, new File(algoTraderPath + "/code/target/classes").getAbsolutePath()));

            // add all jars in the lib directory of this project
            FileSet fileSet = new FileSet();
            fileSet.setDir(new File("lib"));
            fileSet.setIncludes("**/*.jar");
            path.addFileset(fileSet);

            // add all jars in the lib directory of AlgoTrader
            FileSet atFileSet = new FileSet();
            atFileSet.setDir(new File(algoTraderPath + "/code/lib"));
            atFileSet.setIncludes("**/*.jar");
            path.addFileset(atFileSet);

            java.setClasspath(path);

            java.init();
            java.executeJava();

        } catch (BuildException e) {
            e.printStackTrace();
        }
    }
}

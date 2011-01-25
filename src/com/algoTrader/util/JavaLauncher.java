package com.algoTrader.util;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

public class JavaLauncher {

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
                java.createJvmarg().setValue(vmArg);
            }

            FileSet fileset = new FileSet();
            fileset.setDir(new File("lib"));
            fileset.setIncludes("**/*.jar");

            Path path = new Path(project, new File("classes").getAbsolutePath());
            path.addFileset(fileset);

            java.setClasspath(path);

            java.init();
            java.executeJava();

        } catch (BuildException e) {
            e.printStackTrace();
        }
    }
}

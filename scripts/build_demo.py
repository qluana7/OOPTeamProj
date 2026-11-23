#!/usr/bin/env python3
"""Compile Java 17 bytecode and package local design assets in a runnable JAR."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
from zipfile import ZIP_DEFLATED, ZipFile

project = Path(__file__).resolve().parent.parent
configured = Path(os.environ.get('JAVA_HOME', '/nonexistent')) / 'bin' / 'javac'
compiler = str(configured) if configured.is_file() else shutil.which('javac')
if not compiler:
    bundled = Path('/opt/intellij-idea-ultimate-edition/jbr/bin/javac')
    compiler = str(bundled) if bundled.is_file() else None
if not compiler:
    raise SystemExit('JDK 17 이상을 설치하고 JAVA_HOME을 설정해 주세요.')
with tempfile.TemporaryDirectory(prefix='campus-quiz-build-') as build:
    subprocess.run([compiler, '--release', '17', '-encoding', 'UTF-8', '-d', build,
                    str(project / 'src' / 'CampusQuiz.java'),
                    str(project / 'src' / 'CampusRain.java')], check=True)
    with ZipFile(project / 'CampusQuiz-demo.jar', 'w', ZIP_DEFLATED) as jar:
        jar.writestr('META-INF/MANIFEST.MF',
                     'Manifest-Version: 1.0\r\nMain-Class: CampusQuiz\r\n\r\n')
        for path in Path(build).rglob('*.class'):
            jar.write(path, path.relative_to(build))
        for path in (project / 'assets').rglob('*'):
            if path.is_file():
                jar.write(path, path.relative_to(project))
print('Built CampusQuiz-demo.jar with local images and fonts.')

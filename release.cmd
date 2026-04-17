@ECHO OFF

ECHO +++++++++++++++++++++++++++++++  Maven Goal +++++++++++++++++++++++++++++++
ECHO            mvne ctemvn:auslieferung-maven-plugin:release-versions
ECHO -------------------------------  Maven Goal -------------------------------
REM call mvne ctemvn:auslieferung-maven-plugin:help
REM pause
REM kurze Schreibweise, weil im POM das Plugin eingetragen wurde: call mvne auslieferung:release-versions
call mvn ctemvn:auslieferung-maven-plugin:release-versions

pause

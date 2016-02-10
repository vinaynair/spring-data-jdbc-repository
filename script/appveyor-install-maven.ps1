# Source: http://www.yegor256.com/2015/01/10/windows-appveyor-maven.html

$version = $args[0]
$url = "http://www.apache.org/dist/maven/maven-3/${version}/binaries/apache-maven-${version}-bin.zip"
$destDir = "C:\maven"
$mavenHome = "${destDir}\apache-maven-${version}"

echo "Installing Maven $version into $destDir"

Add-Type -AssemblyName System.IO.Compression.FileSystem
if (!(Test-Path -Path "$destDir" )) {
	(new-object System.Net.WebClient).DownloadFile("$url", "C:\maven-bin.zip")
	[System.IO.Compression.ZipFile]::ExtractToDirectory("C:\maven-bin.zip", "$destDir")
}

# Set environment variables.
$env:PATH = "${mavenHome}\bin;${env:JAVA_HOME}\bin;${env:PATH}"
$env:MAVEN_OPTS = "-XX:MaxPermSize=1g -Xmx1g"

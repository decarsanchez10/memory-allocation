$url = "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.4.1/flatlaf-3.4.1.jar"
$output = "flatlaf-3.4.1.jar"

Write-Host "Downloading FlatLaf..."
Invoke-WebRequest -Uri $url -OutFile $output
Write-Host "Download complete: $output"

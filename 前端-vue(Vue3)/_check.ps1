$ErrorActionPreference = "Continue"
Set-Location "c:\Users\KaiXin\Desktop\train"
$output = & npx vue-tsc --build 2>&1 | Out-String
$output | Out-File -FilePath "c:\Users\KaiXin\Desktop\train\_tsc_output.txt" -Encoding UTF8
Write-Host "DONE"
param([int]$interval = 600)

$projectPath = Get-Location
Write-Host "Autocommit started in: $projectPath"
Write-Host "Interval: $interval seconds ($($interval/60) minutes)"
Write-Host "Stop: Ctrl+C"
Write-Host ""

$commitCount = 0

while ($true) {
    try {
        git add -A

        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        $commitMessage = "Auto-commit: $timestamp"

        git commit -m $commitMessage
        $commitExitCode = $LASTEXITCODE
        
        if ($commitExitCode -eq 0) {
            git push origin main
            $pushExitCode = $LASTEXITCODE
            
            if ($pushExitCode -eq 0) {
                $commitCount++
                Write-Host "Commit #$commitCount pushed to GitHub at $(Get-Date -Format 'HH:mm:ss')"
            } else {
                Write-Host "Push failed with exit code: $pushExitCode"
            }
        } else {
            Write-Host "No changes to commit (exit code: $commitExitCode)"
        }
    }
    catch {
        Write-Host "Error: $_"
    }

    Write-Host "Sleeping for $interval seconds..."
    
    # Спим с промежуточными проверками
    for ($i = 0; $i -lt $interval; $i++) {
        Start-Sleep -Seconds 1
        
        # Выводим прогресс каждую минуту
        if ($i -gt 0 -and $i % 60 -eq 0) {
            $remaining = $interval - $i
            Write-Host "Still sleeping... ($i / $interval seconds passed, $remaining seconds remaining)"
        }
    }
    
    Write-Host ""
}

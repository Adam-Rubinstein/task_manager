param([int]$interval = 600)  # 600 сек = 10 минут (по умолчанию)

$projectPath = Get-Location
Write-Host "🚀 Автокоммит запущен в: $projectPath"
Write-Host "⏱️  Интервал: $interval секунд ($($interval/60) минут)"
Write-Host "⏹️  Чтобы остановить: Ctrl+C"
Write-Host ""

$commitCount = 0

while ($true) {
    try {
        # Добавляем все изменения
        git add -A
        
        # Создаём коммит с временной меткой
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        $commitMessage = "Auto-commit: $timestamp"
        
        git commit -m $commitMessage
        
        # Пушим на GitHub
        git push origin main
        
        $commitCount++
        Write-Host "✅ Коммит #$commitCount создан и загружен на GitHub"
        Write-Host "   Время следующего коммита: $(Get-Date -Format 'HH:mm:ss')"
        Write-Host ""
    }
    catch {
        Write-Host "❌ Ошибка: $_" -ForegroundColor Red
    }
    
    # Ждём перед следующим коммитом
    Start-Sleep -Seconds $interval
}

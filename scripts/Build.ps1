#Requires -Version 7.0
[CmdletBinding()]
param([string]$StudioPath,[switch]$PackageOnly)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$versions=Get-Content -LiteralPath (Join-Path $root 'versions.json') -Raw|ConvertFrom-Json
Add-Type -AssemblyName System.IO.Compression.FileSystem
function Zip-Folder([string]$Folder,[string]$Destination){
    $temp=Join-Path (Join-Path $root 'build') ([guid]::NewGuid().ToString()+'.zip')
    [IO.Compression.ZipFile]::CreateFromDirectory($Folder,$temp)
    Copy-Item -LiteralPath $temp -Destination $Destination -Force
}
if(-not $PackageOnly){
    if(-not $StudioPath){$StudioPath=$env:JSS_HOME}
    if(-not $StudioPath){throw 'Informe -StudioPath ou a variável JSS_HOME apontando para o Studio 6.20.6.'}
    $StudioPath=(Resolve-Path -LiteralPath $StudioPath).Path
    $plugins=Join-Path $StudioPath 'plugins'
    if(-not(Test-Path -LiteralPath (Join-Path $plugins 'com.jaspersoft.studio_6.20.6.final.jar'))){throw 'É necessária uma instalação do Studio 6.20.6.'}
    $javaCandidates=@()
    if($env:JAVA_HOME){$javaCandidates+=Join-Path $env:JAVA_HOME 'bin/java.exe'}
    if(Test-Path -LiteralPath (Join-Path $StudioPath 'features')){$javaCandidates+=@(Get-ChildItem -LiteralPath (Join-Path $StudioPath 'features') -Recurse -File -Filter java.exe|ForEach-Object FullName)}
    $java=@($javaCandidates|Where-Object{Test-Path -LiteralPath $_})|Select-Object -First 1
    if(-not $java){$command=Get-Command java -ErrorAction SilentlyContinue;if($command){$java=$command.Source}}
    if(-not $java){throw 'Java 17 ou superior não encontrado.'}
    $compiler=Get-ChildItem -LiteralPath $plugins -File -Filter 'org.eclipse.jdt.core.compiler.batch_*.jar'|Select-Object -First 1
    if(-not $compiler){throw 'Compilador ECJ não encontrado nos plugins do Studio.'}
    $classpath=(Get-ChildItem -LiteralPath $plugins -File -Filter '*.jar'|ForEach-Object{$_.FullName.Replace('\','/')}) -join [IO.Path]::PathSeparator
    $specs=@(@{Name='theme';Version=$versions.theme;Id='com.salutem.jasper.theme.deepblue'},@{Name='language';Version=$versions.language;Id='com.salutem.jasper.language.pt_BR'},@{Name='connection-order';Version=$versions.connectionOrder;Id='com.salutem.jasper.connectionorder'})
    foreach($spec in $specs){
        $module=Join-Path $root ('modules/'+$spec.Name)
        $stage=Join-Path $root ('build/compile-'+$spec.Name+'-'+[guid]::NewGuid())
        New-Item -ItemType Directory -Force -Path $stage|Out-Null
        Get-ChildItem -LiteralPath (Join-Path $module 'resources')|ForEach-Object{Copy-Item -LiteralPath $_.FullName -Destination $stage -Recurse}
        $argfile=Join-Path $stage 'compiler.args'
        $arguments=@('-17','-encoding','UTF-8','-classpath',('"'+$classpath+'"'),'-d',('"'+$stage.Replace('\','/')+'"'))
        $arguments+=@(Get-ChildItem -LiteralPath (Join-Path $module 'src') -Recurse -File -Filter '*.java'|ForEach-Object{'"'+$_.FullName.Replace('\','/')+'"'})
        [IO.File]::WriteAllText($argfile,($arguments -join "`n"),(New-Object Text.UTF8Encoding($false)))
        & $java -jar $compiler.FullName ('@'+$argfile)
        if($LASTEXITCODE -ne 0){throw "Compilação falhou: $($spec.Name)"}
        # Compiler arguments are build metadata, not part of the plug-in.
        $bundle=Join-Path $root ('build/bundle-'+$spec.Name+'-'+[guid]::NewGuid())
        New-Item -ItemType Directory -Force -Path $bundle|Out-Null
        Get-ChildItem -LiteralPath $stage|Where-Object{$_.Name -ne 'compiler.args'}|ForEach-Object{Copy-Item -LiteralPath $_.FullName -Destination $bundle -Recurse}
        $mf=Join-Path $bundle 'META-INF/MANIFEST.MF'
        $text=[IO.File]::ReadAllText($mf) -replace '(?m)^Bundle-Version: [^\r\n]+',('Bundle-Version: '+$spec.Version)
        [IO.File]::WriteAllText($mf,$text,[Text.Encoding]::ASCII)
        Zip-Folder $bundle (Join-Path $root ('artifacts/plugins/'+$spec.Id+'_'+$spec.Version+'.jar'))
    }
    foreach($fragment in Get-ChildItem -LiteralPath (Join-Path $root 'translations/fragments') -Directory){
        $stage=Join-Path $root ('build/fragment-'+[guid]::NewGuid());Copy-Item -LiteralPath $fragment.FullName -Destination $stage -Recurse
        $mf=Join-Path $stage 'META-INF/MANIFEST.MF';$text=[IO.File]::ReadAllText($mf) -replace '(?m)^Bundle-Version: [^\r\n]+',('Bundle-Version: '+$versions.translationFragments);[IO.File]::WriteAllText($mf,$text,[Text.Encoding]::ASCII)
        Zip-Folder $stage (Join-Path $root ('artifacts/plugins/'+$fragment.Name+'_'+$versions.translationFragments+'.jar'))
    }
    & node (Join-Path $PSScriptRoot 'Verify.mjs') --snapshot
    if($LASTEXITCODE -ne 0){throw 'Verificação dos fontes e artefatos falhou.'}
}else{
    & node (Join-Path $PSScriptRoot 'Verify.mjs')
    if($LASTEXITCODE -ne 0){throw 'Artefatos desatualizados. Faça um build completo.'}
}
$snapshot=Get-Content -LiteralPath (Join-Path $root 'artifacts/index.json') -Raw|ConvertFrom-Json
$stage=Join-Path $root ('build/package-'+[guid]::NewGuid())
$package=Join-Path $stage 'Tradução e Tema JS'
New-Item -ItemType Directory -Force -Path (Join-Path $package 'pacote/plugins'),(Join-Path $package 'fontes'),(Join-Path $root 'dist')|Out-Null
Get-ChildItem -LiteralPath (Join-Path $root 'installer')|ForEach-Object{Copy-Item -LiteralPath $_.FullName -Destination $package -Recurse}
$inventory=@()
foreach($entry in $snapshot.files){$source=Join-Path $root $entry.path;Copy-Item -LiteralPath $source -Destination (Join-Path $package 'pacote/plugins');$inventory+=[pscustomobject]@{name=[IO.Path]::GetFileName($source);sha256=$entry.sha256.ToUpperInvariant()}}
$utf8=New-Object Text.UTF8Encoding($true)
[IO.File]::WriteAllText((Join-Path $package 'pacote/arquivos.json'),($inventory|ConvertTo-Json -Depth 3),$utf8)
foreach($dir in @('modules','translations','scripts','tests')){Copy-Item -LiteralPath (Join-Path $root $dir) -Destination (Join-Path $package 'fontes') -Recurse}
Copy-Item -LiteralPath (Join-Path $root 'versions.json'),(Join-Path $root 'README.md'),(Join-Path $root 'THIRD_PARTY_NOTICES.md') -Destination $package
Copy-Item -LiteralPath (Join-Path $root 'docs/INSTALACAO.md') -Destination (Join-Path $package 'LEIA-ME.md')
$artifact=Join-Path $root ('dist/Traducao-e-Tema-JS-v'+$versions.packageVersion+'.zip')
Zip-Folder $stage $artifact
$sha=(Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash.ToLowerInvariant()
[IO.File]::WriteAllText((Join-Path $root 'dist/SHA256SUMS.txt'),($sha+'  '+[IO.Path]::GetFileName($artifact)+"`n"),[Text.Encoding]::ASCII)
Write-Output "Pacote criado: $artifact"

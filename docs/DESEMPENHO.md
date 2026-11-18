# Otimizações 2.6.0

A revisão atua nos complementos e no inicializador. Mantém a configuração da JVM do produto e as consultas dos relatórios.

| Rotina medida | Antes | Depois | Método |
|---|---:|---:|---|
| Preparação do inicializador | 1.519 ms | 72,9 ms | Média de 3 execuções no mesmo computador, comparando Get-Plan e Get-LaunchPlan após aquecimento |
| 200 mil consultas de rótulos | 108,67 ms | 6,25 ms | TranslationBenchmark, processos separados, 20 mil consultas de aquecimento, checksum idêntico |

Esses valores não representam tempo total de abertura do Studio, consumo total de CPU, execução SQL nem geração de relatórios. Não foi feito benchmark de relatórios reais.

## Funcionamento

A instalação valida os hashes e os módulos como antes. A abertura confere apenas o núcleo 6.20.6, Java 17+ e metadados dos JARs instalados. Um estado local por pasta do inicializador/instalação guarda a assinatura de nomes, tamanhos e datas dos complementos. Primeira abertura, estado inválido ou mudança de assinatura inclui -clean; aberturas seguintes reutilizam o cache do Eclipse. Nenhum cache do workspace ou relatório é apagado.

A ordenação não é mais executada a cada tecla/clique. Continua nos menus, em alterações do armazenamento agrupadas por 100 ms e numa verificação de segurança a cada 5 segundos. Mapas que já estão ordenados são verificados numa passagem, sem cópias e sem nova ordenação.

Tema e tradução reagem a eventos de exibição/seleção/foco e agrupam atualizações numa janela de 100 ms. Pedidos para pais e filhos são consolidados. Controles e menus ocultos não entram na varredura periódica; são tratados ao aparecer. Há uma varredura de segurança de áreas visíveis a cada 3 segundos para atualizações assíncronas que não emitem esses eventos. O tema também trata modificações nos editores.

O cache de tradução tem no máximo 2.048 entradas. Cores são reutilizadas entre as sete paletas e liberadas ao fechar o Display. Não são guardadas credenciais nem alterados modelos de relatórios.

## Reproduzir

- Teste do inicializador: powershell.exe -NoProfile -ExecutionPolicy Bypass -File tests/CheckLauncher.ps1
- Medir a preparação local: acrescente -StudioPath apontando para o Studio e -PackageRoot apontando para a pasta Traducao-ptBR gerada pelo build.
- Compile os testes Java com os módulos e dependências do Studio 6.20.6. CheckRefreshQueue testa agrupamento e controles ocultos; CheckThemeSwitch testa sete paletas e reaproveitamento das cores; TranslationBenchmark mede somente consultas de rótulos.

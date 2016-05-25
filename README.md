# POOStreaming
Trabalho da disciplina Programação Orientada a Objetos
<br>

Integrantes:
Rafael Augusto Monteiro - 9293095
Lucas Alexandre Soares - 9293265
Giovanna Oliveira Guimaraes - 9293693

Proposta:
Um software de streaming de video. Consiste em um servidor que comprime(caso necessário) um video e o envia para um cliente, que executa a midia enviada. O servidor será comandado via linha de comando, enquanto o cliente terá uma interface gráfica. Será utilizada a API JavaFX para a criação do cliente reprodutor de mídia. 

O que o software deve fazer:
Servidor: Deve responder à solicitações do cliente. Irá fornecer ao cliente uma lista com os arquivos de video disponíveis, realizar os tratamentos necessários e enviar o video para uma aplicação cliente.
Cliente: Deve soliciar arquivos de mídia ao servidor e reproduzí-los. Irá soliciar ao servidor uma lista de arquivos de video disponíveis, selecionar um arquivo, recebê-lo do servidor e reproduzí-lo. Os arquivos utilizados pela reprodução não serão (a priore) armazenados em disco, mas a implementação pode utilizar arquivos temporários se necessário. Terá uma interface gráfica para facilitar as solicitações.

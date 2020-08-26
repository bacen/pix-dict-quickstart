## *DICT - QuickStart*

Bem-vindo ao QuickStart do DICT, o Diretório de Identificadores de Contas Transacionais
do PIX (Sistema de Pagamentos Instantâneos do Banco Central do Brasil). O objetivo 
é guiar o usuário/desenvolvedor no primeiro contato com a API do DICT. O público-alvo
são profissionais de TI dos Prestadores de Serviços de Pagamentos (PSP). Se você
não faz parte desse grupo, provavelmente o código aqui apresentado não servirá a 
nenhum propósito.

A aplicação gerada nesse guia só irá funcionar se for executada a partir de uma máquina
conectada à RSFN (Rede do Sistema Financeiro Nacional), e com certificados ISPB válidos
e registrados previamente para fins de realização de Pagamentos Instantâneos.

Se você tiver alguma dúvida, ou se esse é seu primeiro contato com o PIX ou com 
o  DICT, recomendamos que verifique a página oficial do ecossitema de 
[pagamentos instantâneos](https://www.bcb.gov.br/estabilidadefinanceira/forumpagamentosinstantaneos),
especialmente: Especificações Técnicas e de Negócio; Manual das Interfaces de Comunicação; 
Manual de Conectividade com a RSFN; e o Manual de Segurança.

Esse projeto é disponibilizado com propósito didático, e não deve ser utilizado 
como base para construção da integração final do PSP com o DICT. Ele não dá soluções
'robustas' para questões como: gestão de certificados na abertura do canal TLS, ou 
fluxos de tratamento de falhas nas operações.

O projeto é disponibilizado sob a licença APACHE 2.0 (arquivo LICENSE). Ao baixar 
o projeto você está concordando com os termos da licença. Em resumo: não nos responsabilizamos 
por nenhum problema decorrente do uso desse código! Utilize-o por sua própria conta 
e risco.

### Observações:

- O DICT utiliza autenticação TLS mútua: o cliente também precisa se autenticar.
  Verifique o processo de registro do certificado no documento "Especificações Técnicas 
  e de Negócio do Ecossistema de Pagamentos Instantâneos Brasileiro".

- No quickstart, para simplificar, os objetos criptográficos (certificado e chave
  privada) são armazenados em arquivos, mas isso não deve ser replicado no ambiente 
  de produção! Recomendamos a utilização de um hardware security module (HSM) para 
  gerenciar o acesso a essas informações.
  
- O processo de assinatura digital das requisições ao DICT é exemplificado no quickstart. 
No entanto, as assinaturas das respostas do DICT **não estão sendo validadas**. Para produção,
é fundamental que as assinaturas digitais sejam corretamente validadas.

- Para pleno funcionamento e geração correta das classes cliente, matenha as versões 
  utilizadas neste exemplo.

- A documentação oficial da API e do DICT podem ser consultadas no [projeto de interface do DICT](https://github.com/bacen/pix-dict-api)

### Tecnologias utilizadas:
```
Java 8 (jdk8u232-b09) 
Maven 3.6.2
```

### Passo a passo:

- Baixe a aplicação com o seguinte comando:
```
git clone https://github.com/bacen/pix-dict-quickstart.git
```

- Dentro da pasta do projeto, execute:
```
 mvn package
```

*No passo acima, as classes necessárias são criadas a partir da especificação (spec.yaml)*
 
Para executar a aplicação, deve-se informar a localização e a senha dos objetos criptográficos, bem como o ISPB do participante.
Nesse exemplo são usados dois keystores distintos: um para conexão TLS e outro passa assinatura digital. 

```
java -Djavax.net.ssl.keyStore=${PWD}/channel.pfx \
     -Djavax.net.ssl.keyStorePassword=changeit \
     -jar target/dict-quickstart.jar \
     -ispb <ispb-do-participante> \
     -signatureKeyStore signature.pfx \
     -signatureKeyStorePassword changeit \
     -baseAddress https://dict-h.pi.rsfn.net.br/api/v1-rc6
```
 

### Problemas e Soluções

|                                                          Problema                                                         |                                  Solução                                  |
|:--------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------|
| sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target | Deve-se importar o certicado da AC Raiz (v5) da ICP-Brasil para a keystore da JVM que está executando a aplicação |
| javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure| Na execução, deve-se fornecer a localização e a senha de acesso do arquivo que armazena o certificado e a chave privada| 
| Entry associated with given key does not exist | Verifique se o ISPB do participante foi definido corretamente |
| Entry in custody of participant. Use your book transfer | O participante não pode consultar as chaves que estão sob sua custódia |  
| Participant is not allowed to access this resource | Verifique se o ISPB informado na linha de comando é o mesmo do certificado |
| Could not get content org.apache.http.conn.ConnectTimeoutException: Connect to github.com:443 | O Maven não está conseguindo baixar o arquivo spec.yaml do github. Você provavelmente está atrás de um proxy. Se for esse o caso, informe o proxy na linha de comando ```mvn clean install -Dhttp.proxyHost=12.23.34.45 -Dhttp.proxyPort=1234 -Dhttps.proxyHost=12.23.34.45 -Dhttps.proxyPort=1234```. Opcionalmente, baixe manualmente o arquivo 'spec.yaml' disponível em https://github.com/bacen/pix-dict-api e salve na pasta src/main/resources, e execute o mvn da seguinte forma: ```mvn clean install -Ddownload.plugin.skip=true```


### Exemplos

- Importar o certificado da AC ICPBR, usando shell BASH, no Linux:

```
keytool -import \
        -trustcacerts \
        -keystore ${JAVA_HOME}/jre/lib/security/cacerts \
        -storepass changeit \
        -alias acraiz-icpbr \
        -file  <(curl -sk http://acraiz.icpbrasil.gov.br/credenciadas/RAIZ/ICP-Brasilv5.crt)
```

- Gerar keystores PKCS#12 (signature.pfx e channel.pfx) importando o certificado e a chave no formato PEM:
 
```
openssl pkcs12 -export -in client.crt -inkey client.key -out signature.pfx -name client -password pass:changeit
```

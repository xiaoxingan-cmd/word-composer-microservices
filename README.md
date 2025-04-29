# WordComposerMicroservice

> [!CAUTION]
> Это тестовый проект сделанный мною для теста парочки технологий и использовать его я не рекомендую 😃
>
>  Архитектуру стоило бы поправить и убрать сохранение статуса объекта на каждом микросервисе и, соответственно, лишние вызовы Utility-сервисов (S3/Database), которые могут сильно замедлять быстродействие. В идеале объект должен сохраняться только один раз, когда он будет полностью собран.

> [!NOTE]  
> * [Demo video](https://drive.google.com/file/d/1z0o_aCw4Ef8Z3Lf-KmbfwyD-yiEkTtIk/view)
> * [WCM Scheme](https://www.figma.com/board/NAss9AIxLZl4a5mzyvPmw3/WCM-Scheme?node-id=27-296&t=xo68IR39sDgCMfbt-4)

> [!NOTE]  
> Данное приложение помогает обрабатывать **англоязычные** слова, которые в последствии получают **перевод, транскрипцию, значение, а также изображение для создания карточки слова**.
>
> ![example-woman](examples/af96fdcc3502.jpg)
>
> ![example-sea](examples/a562fd6c125e.jpg)

> [!TIP]
> Используется следующее:
> * [GITHUB CLOUD CONFIG SETTINGS](https://github.com/hannahmontana-554/word-composer-microservices/tree/master/spring_cloud_config_settings-master)
> * API KEYS_SECRETS -> (https://dictionaryapi.dev/, https://unsplash.com/, https://imgbb.com/)
> * S3 Container
> * Self-hosted LibreTranslate in Docker
> * Postgresql DB
> * [Elastic-Stack for logging (Optional)](https://github.com/hannahmontana-554/elk-stack)
> * minikube -> strimzi config with 3 kafka topics: initial-topic, image-handler-topic, card-representor-topic

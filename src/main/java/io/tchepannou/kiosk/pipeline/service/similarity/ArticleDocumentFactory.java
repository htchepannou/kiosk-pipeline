package io.tchepannou.kiosk.pipeline.service.similarity;

public class ArticleDocumentFactory {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleDocumentFactory.class);
//
//    @Autowired
//    AmazonS3 s3;
//
//    private String s3Bucket;
//
//    public Document createDocument(final Article article){
//        try {
//            final String content = loadContent(article);
//            return new Document() {
//                @Override
//                public long getId() {
//                    return article.getId();
//                }
//
//                @Override
//                public String getContent() {
//                    return content;
//                }
//            };
//        }catch (Exception e){
//            LOGGER.error("Unable to fetch content", e);
//            return null;
//        }
//    }
//
//    private String loadContent (final Article article) throws IOException {
//        final String key = article.getS3Key();
//        LOGGER.info("Fetching content from s3://{}/{}", s3Bucket, key);
//        try (S3Object obj = s3.getObject(s3Bucket, key)){
//            final String html = IOUtils.toString(obj.getObjectContent());
//            return Jsoup.parse(html).text();
//        }
//
//    }
//
//    public AmazonS3 getS3() {
//        return s3;
//    }
//
//    public void setS3(final AmazonS3 s3) {
//        this.s3 = s3;
//    }
//
//    public String getS3Bucket() {
//        return s3Bucket;
//    }
//
//    public void setS3Bucket(final String s3Bucket) {
//        this.s3Bucket = s3Bucket;
//    }
}

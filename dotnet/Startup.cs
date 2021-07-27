using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.OpenApi.Models;
using OpenTelemetry.Trace;
using OpenTelemetry.Resources;
using OpenTelemetry.Exporter;

namespace dotnet
{
    class EventHubConfigurationOptions
    {
        public EventHubConfigurationOptions() 
        {
        }
        public string ConnectionString { get; set; }
        public string EventHubName { get; set; }
    }

    class StorageConfigurationOptions
    {
        public StorageConfigurationOptions()
        {
        }
        public string ConnectionString { get; set; }
        public string BlobContainerName { get; set; }
    }


    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddControllers();
            services.AddHttpClient();
            services.Configure<EventHubConfigurationOptions>(this.Configuration.GetSection("EventHub"));
            services.Configure<StorageConfigurationOptions>(this.Configuration.GetSection("Storage"));
            services.AddHostedService<EventProcessor>();

            var exporter = this.Configuration.GetValue<string>("TELEMETRY_DESTINATION").ToLowerInvariant();
            switch (exporter)
            {
                case "jaeger":
                    services.AddOpenTelemetryTracing((builder) => builder
                        .SetResourceBuilder(ResourceBuilder.CreateDefault().AddService(this.Configuration.GetValue<string>("Jaeger:ServiceName")))
                        .AddAspNetCoreInstrumentation()
                        .AddHttpClientInstrumentation()
                        .AddLegacySource("EventHubs.Message")
                        .AddLegacySource("EventHubProducerClient.Send")
                        .AddLegacySource("EventProcessor.Process")
                        .AddLegacySource("EventProcessor.Checkpoint")
                        .AddJaegerExporter());

                    services.Configure<JaegerExporterOptions>(this.Configuration.GetSection("Jaeger"));
                    break;
                case "applicationinsightssdk":
                    services.AddApplicationInsightsTelemetry();
                    break;
                default:
                    services.AddOpenTelemetryTracing((builder) => builder
                        .AddAspNetCoreInstrumentation()
                        .AddHttpClientInstrumentation()
                        .AddConsoleExporter()); ;
                    break;
            }
            services.AddSwaggerGen(c =>
            {
                c.SwaggerDoc("v1", new OpenApiInfo { Title = "dotnet", Version = "v1" });
            });
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
                app.UseSwagger();
                app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "dotnet v1"));
            }


            app.UseRouting();

            app.UseAuthorization();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapControllers();
            });
        }
    }
}

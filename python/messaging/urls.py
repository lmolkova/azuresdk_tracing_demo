from django.urls import path

from . import views

urlpatterns = [
    path('eventhub', views.eventhub, name='eventhub'),
    path('servicebus/message/<str:message>', views.servicebus, name='servicebus'),
]
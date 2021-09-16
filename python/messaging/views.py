from django.shortcuts import render
from tracingdemo.tracing.eventhubs import receive_messages

# Create your views here.
from django.http import HttpResponse


def index(request):
    messages = receive_messages()
    return HttpResponse("Hello, world." + messages)

from django.shortcuts import render
from tracingdemo.tracing.eventhubs import receive_messages
from tracingdemo.tracing.servicebus import send_messages

# Create your views here.
from django.http import HttpResponse


def eventhub(request):
    messages = receive_messages()
    return HttpResponse("Hello, world." + messages)

def servicebus(request, message):
    txt = send_messages(message)
    return HttpResponse(txt)